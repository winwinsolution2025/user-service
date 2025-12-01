package com.example.userservice.infrastructure.controller;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.exception.InvalidParameterException;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.usecase.impl.*;
import com.example.userservice.dto.*;
import io.javalin.http.Handler;

import java.util.List;
import java.util.stream.Collectors;

public class UserController {
    private final GetUserUseCase getUserUseCase;
    private final GetMeUseCase getMeUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateMeUseCase updateMeUseCase;

    public final Handler getMe;
    public final Handler updateMe;
    public final Handler getUser;
    public final Handler updateUser;
    public final Handler addUser;
    public final Handler deleteUser;
    public final Handler listUsers;

    public UserController(GetUserUseCase getUserUseCase,
                          GetMeUseCase getMeUseCase,
                          CreateUserUseCase createUserUseCase,
                          ListUsersUseCase listUsersUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          UpdateMeUseCase updateMeUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.getMeUseCase = getMeUseCase;
        this.createUserUseCase = createUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.updateMeUseCase = updateMeUseCase;


        listUsers = (ctx) -> {
            List<UserResponse> response = this.listUsersUseCase.execute().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            ctx.json(new ErrorResponse(response));
        };

        getMe = (ctx) -> {
            var user = this.getMeUseCase.execute(ctx.attribute("authenticatedEmail"));

            ctx.json(new ErrorResponse(mapToResponse(user)));
        };

        updateMe = (ctx) -> {
            //handle null
            var email = ctx.attribute("authenticatedEmail").toString();
            UpdateMeRequest request = ctx.bodyAsClass(UpdateMeRequest.class);
            var response = this.updateMeUseCase.execute(email, request).map(this::mapToResponse);

            ctx.json(new ErrorResponse(response));
        };

        updateUser = (ctx) -> {
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
                this.updateUserUseCase.execute(id, request).map(this::mapToResponse);

                ctx.status(204);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        };

        getUser = (ctx) -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));
                var result = this.getUserUseCase.execute(id);
                if (result.isEmpty()) {
                    throw new NotFoundException("User", id);
                }

                ctx.json(new ErrorResponse(result.get()));
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid ID format: ID must be a number");
            }
        };

        addUser = (ctx) -> {
            CreateUserRequest request = ctx.bodyAsClass(CreateUserRequest.class);

            if (request.getName() == null || request.getName().isEmpty()) {
                throw new InvalidParameterException("Name is required");
            }

            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                throw new InvalidParameterException("Email is required");
            }

            User user = this.createUserUseCase.execute(request);
            var response = mapToResponse(user);
            ctx.status(201).json(new ErrorResponse(response));
        };

        deleteUser = ctx -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));
                this.deleteUserUseCase.execute(id);
                ctx.status(204).result("");
            } catch (NumberFormatException e) {
                ctx.status(400).json(new ErrorResponse("INVALID_ID_FORMAT", "Invalid ID format: ID must be a number"));
            }
        };

    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUUID(user.getUUID());
        response.setName(user.getName());
        response.setGender(user.getGender());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setBirthdate(user.getBirthdate());
        response.setEmail(user.getEmail());
        return response;
    }
}
