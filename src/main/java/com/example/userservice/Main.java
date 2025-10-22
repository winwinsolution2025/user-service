package com.example.userservice;

import com.example.userservice.config.Config;
import com.example.userservice.config.PersistenceConfig;
import com.example.userservice.domain.exception.HttpException;
import com.example.userservice.domain.exception.InvalidParameterException;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.usecase.impl.*;
import com.example.userservice.dto.*;
import com.example.userservice.infrastructure.controller.UserController;
import com.example.userservice.infrastructure.middleware.AuthMiddleware;
import com.example.userservice.infrastructure.repository.MySQLUserRepository;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.json.JavalinJackson;
import io.javalin.plugin.bundled.CorsPluginConfig;
import jakarta.persistence.EntityManagerFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLIntegrityConstraintViolationException;

import static com.example.userservice.Main.HttpStatus.*;

public class Main {
    private static EntityManagerFactory emf;

    public static void main(String[] args) throws Exception {
        Config.load();
        migrate();

        emf = PersistenceConfig.createEntityManagerFactory();
        var userController = getUserController();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                // Allow all origins
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });
            config.jsonMapper(new JavalinJackson(JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build(), true));
        });

        // Add custom error handler

        // parse request exception
        app.exception(UnrecognizedPropertyException.class, (e, ctx) -> {
            throw new InvalidParameterException(e.getMessage());
        });

        app.exception(HttpException.class, (e, ctx) -> {
            ctx.status(e.getStatus());
            ctx.json(new ErrorResponse(getStatusMessage(e.getStatus()), e.getMessage()));
        });

        app.exception(HttpResponseException.class, (e, ctx) -> {
            ctx.status(e.getStatus());
            ctx.json(new ErrorResponse(getStatusMessage(e.getStatus()), e.getMessage()));
        });

        app.exception(ConstraintViolationException.class, (e, ctx) -> {
            Throwable cause = e.getCause();
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                ctx.status(CONFLICT_RESOURCE);
                ctx.json(new ErrorResponse(getStatusMessage(CONFLICT_RESOURCE), e.getMessage()));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(new ErrorResponse(getStatusMessage(CONFLICT_RESOURCE), e.getMessage()));
            }
        });


        // Apply authentication middleware for all path
        app.before(AuthMiddleware.authenticate);

        // Define routes
        String prefix = "/api/v1";
        app.post(prefix + "/users", ctx -> {
            CreateUserRequest request = ctx.bodyAsClass(CreateUserRequest.class);

            if (request.getName() == null || request.getName().isEmpty()) {
                throw new InvalidParameterException("Name is required");
            }

            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                throw new InvalidParameterException("Email is required");
            }

            UserResponse response = userController.handleAddUser(request);
            ctx.status(201).json(response);
        });

        app.get(prefix + "/users/me", ctx -> {
            userController.handleGetMe(ctx.attribute("authenticatedEmail")).ifPresentOrElse(
                    ctx::json,
                    () -> {
                        throw new NotFoundException("User", ctx.attribute("authenticatedEmail"));
                    }
            );
        });

        app.get(prefix + "/users/{id}", ctx -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));
                var result = userController.handleGetUser(id);
                if (result.isPresent()) {
                    ctx.json(result.get());
                } else {
                    throw new NotFoundException("User", id);
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid ID format: ID must be a number");
            }
        });

        app.put(prefix + "/users/me", ctx -> {
            UpdateMeRequest request = ctx.bodyAsClass(UpdateMeRequest.class);

            userController.handleUpdateMe(ctx.attribute("authenticatedEmail"), request).ifPresent(
                    ctx::json);

        });

        app.get(prefix + "/users", ctx -> {
            ctx.json(userController.handleListUsers());
        });

        app.delete(prefix + "/users/{id}", ctx -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));
                if (userController.handleDeleteUser(id)) {
                    ctx.status(204).result("");
                } else {
                    throw new NotFoundException("User", id);
                }
            } catch (NumberFormatException e) {
                ctx.status(400).json(new ErrorResponse("INVALID_ID_FORMAT", "Invalid ID format: ID must be a number"));
            }
        });

        app.put(prefix + "/users/{id}", ctx -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));
                UpdateUserRequest request = ctx.bodyAsClass(UpdateUserRequest.class);

                if (request.getName() == null || request.getName().isEmpty()) {
                    ctx.status(400).json(new ErrorResponse("INVALID_REQUEST", "Name is required"));
                    return;
                }

                if (request.getEmail() == null || request.getEmail().isEmpty()) {
                    ctx.status(400).json(new ErrorResponse("INVALID_REQUEST", "Email is required"));
                    return;
                }

                userController.handleUpdateUser(id, request).ifPresent(
                        ctx::json);
            } catch (NumberFormatException e) {
                ctx.status(400).json(new ErrorResponse("INVALID_ID_FORMAT", "Invalid ID format: ID must be a number"));
            }
        });

        // Start the server
        app.start(Config.PORT);
        System.out.println("Server started on port " + Config.PORT);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emf != null) {
                emf.close();
            }
            app.stop();
        }));
    }

    @NotNull
    private static UserController getUserController() {
        var repository = new MySQLUserRepository(emf);

        var getUserUseCase = new GetUserUseCase(repository);
        var getMeUseCase = new GetMeUseCase(repository);
        var createUserUseCase = new CreateUserUseCase(repository);
        var listUsersUseCase = new ListUsersUseCase(repository);
        var deleteUserUseCase = new DeleteUserUseCase(repository);
        var updateUserUseCase = new UpdateUserUseCase(repository);
        var updateMeUseCase = new UpdateMeUseCase(repository);

        var userController = new UserController(
                getUserUseCase,
                getMeUseCase,
                createUserUseCase,
                listUsersUseCase,
                deleteUserUseCase,
                updateUserUseCase,
                updateMeUseCase
        );
        return userController;
    }

    public static String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case BAD_REQUEST -> "BAD_REQUEST";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case CONFLICT_RESOURCE -> "CONFLICT_RESOURCE";
            case INTERNAL_SERVER_ERROR -> "INTERNAL_SERVER_ERROR";
            default -> "UNKNOWN";
        };
    }

    public static class HttpStatus {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;

        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int CONFLICT_RESOURCE = 409;

        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    static void migrate() {
        try (Connection connection = DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASS)) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Migration failed", e);
        }
    }
}
