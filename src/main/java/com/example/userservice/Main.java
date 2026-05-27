package com.example.userservice;

import com.example.userservice.config.AppConfig;
import com.example.userservice.domain.exception.HttpException;
import com.example.userservice.domain.exception.InvalidParameterException;
import com.example.userservice.domain.usecase.impl.*;
import com.example.userservice.dto.ErrorResponse;
import com.example.userservice.infrastructure.controller.UserController;
import com.example.userservice.infrastructure.middleware.AuthMiddleware;
import com.example.userservice.infrastructure.repository.MySQLUserRepository;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.json.JavalinJackson;
import io.javalin.plugin.bundled.CorsPluginConfig;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLIntegrityConstraintViolationException;

import static com.example.userservice.Main.HttpStatus.*;

public class Main {
    private static final Logger appLog = LoggerFactory.getLogger(Main.class);
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.GetInstance();
        migrate(config);

        Validator validator = factory.getValidator();


        DataSource ds = createHikariDataSource(config);
        DSLContext dbCtx = DSL.using(ds, SQLDialect.MYSQL);
        var userController = getUserController(dbCtx, validator);

        Javalin app = Javalin.create(conf -> {
            conf.bundledPlugins.enableCors(cors -> {
                // Allow all origins
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });
            conf.jsonMapper(new JavalinJackson(JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .addModule(new Jdk8Module())
                    .build(), true));
        });

        // Add custom error handler

        // parse request exception


        app.exception(HttpException.class, (e, ctx) -> {
            addCorsHeaders(ctx);
            ctx.status(e.getStatus());
            ctx.json(new ErrorResponse(getStatusMessage(e.getStatus()), e.getMessage()));
        });

        app.exception(UnrecognizedPropertyException.class, (e, ctx) -> {
            throw new InvalidParameterException(e.getMessage());
        });

        app.exception(HttpResponseException.class, (e, ctx) -> {
            addCorsHeaders(ctx);
            appLog.error("call service exception!", e);
            ctx.status(e.getStatus());
            ctx.json(new ErrorResponse(getStatusMessage(e.getStatus()), e.getMessage()));
        });

        app.exception(DataAccessException.class, (e, ctx) -> {
            addCorsHeaders(ctx);
            Throwable cause = e.getCause();
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                ctx.status(CONFLICT_RESOURCE);
                ctx.json(new ErrorResponse(getStatusMessage(CONFLICT_RESOURCE), e.getMessage()));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(new ErrorResponse(getStatusMessage(CONFLICT_RESOURCE), e.getMessage()));
            }
        });

        app.exception(RuntimeException.class, (e, ctx) -> {
            addCorsHeaders(ctx);
            appLog.error("run time exception!", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse(getStatusMessage(HttpStatus.INTERNAL_SERVER_ERROR), e.getMessage()));
        });


        // Apply authentication middleware for all path

        app.before(ctx -> {
            if (ctx.method().equals(io.javalin.http.HandlerType.OPTIONS)) return;
            AuthMiddleware.authenticate.handle(ctx);
        });

        String prefix = "/api/v1";
        // Define routes
        app.post(prefix + "/users", userController.addUser);

        app.get(prefix + "/users/me", userController.getMe);

        app.get(prefix + "/users/{id}", userController.getUser);

        app.put(prefix + "/users/me", userController.updateMe);

        app.get(prefix + "/users", userController.listUsers);

        app.delete(prefix + "/users/{id}", userController.deleteUser);

        app.put(prefix + "/users/{id}", userController.updateUser);

        // Start the server
        app.start("0.0.0.0", config.getPort());
        System.out.println("Server started on port " + config.getPort());

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
        }));
    }

    @NotNull
    private static UserController getUserController(DSLContext dbCtx, Validator validator) {
        var repository = new MySQLUserRepository(dbCtx);

        var getUserUseCase = new GetUserUseCase(repository);
        var getMeUseCase = new GetMeUseCase(repository);
        var createUserUseCase = new CreateUserUseCase(repository);
        var listUsersUseCase = new ListUsersUseCase(repository);
        var deleteUserUseCase = new DeleteUserUseCase(repository);
        var updateUserUseCase = new UpdateUserUseCase(repository);
        var updateMeUseCase = new UpdateMeUseCase(repository);

        var userController = new UserController(
                validator,
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

    static void migrate(AppConfig config) {
        try (Connection connection = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPass())) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Migration failed", e);
        }
    }
    private static void addCorsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Headers", "*");
        ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }

    private static DataSource createHikariDataSource(AppConfig cfg) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(cfg.getDbUrl()); // replace with your DB
        config.setUsername(cfg.getDbUser());
        config.setPassword(cfg.getDbPass());

        // Optional tuning
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30s
        config.setIdleTimeout(600000);      // 10 min
        config.setMaxLifetime(1800000);     // 30 min

        // Create the HikariCP DataSource
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }
}
