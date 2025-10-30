package com.example.userservice.infrastructure.controller;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.exception.InvalidParameterException;
import com.example.userservice.domain.usecase.impl.*;
import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UpdateMeRequest;
import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.dto.UserResponse;
import io.javalin.http.Handler;

import java.util.stream.Collectors;

public class UserController {
    private final GetUserUseCase getUserUseCase;
    private final GetMeUseCase getMeUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateMeUseCase updateMeUseCase;

    public final Handler getUser;
    public final Handler getMe;
    public final Handler getUsers;
    public final Handler addUser;
    public final Handler updateUser;
    public final Handler updateMe;
    public final Handler deleteUser;

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

        this.getMe = ctx -> {
            var user = this.getMeUseCase.execute(ctx.attribute("authenticatedEmail"));

            ctx.json(this.mapToResponse(user));
        };
        this.getUser = ctx -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));

                var user = this.getUserUseCase.execute(id);
                ctx.json(this.mapToResponse(user));

            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid ID format: ID must be a number");
            }
        };
        this.getUsers = ctx -> {
            var response = this.listUsersUseCase.execute().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            ctx.json(response);
        };
        this.addUser = ctx -> {
            CreateUserRequest request = ctx.bodyAsClass(CreateUserRequest.class);
            User user = this.createUserUseCase.execute(request);
            ctx.json(this.mapToResponse(user));
        };
        this.updateUser = ctx -> {
            try {
                Integer id = Integer.parseInt(ctx.pathParam("id"));
                UpdateUserRequest request = ctx.bodyAsClass(UpdateUserRequest.class);
                if (request.getName() == null || request.getName().isEmpty()) {
                    throw new InvalidParameterException("Name is required");
                }

                if (request.getEmail() == null || request.getEmail().isEmpty()) {
                    throw new InvalidParameterException("Email is required");
                }

                var user = updateUserUseCase.execute(id, request);
                ctx.json(this.mapToResponse(user));

            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid ID format: ID must be a number");
            }

        };
        this.updateMe = ctx -> {
            var email = ctx.attribute("authenticatedEmail").toString();
            UpdateMeRequest request = ctx.bodyAsClass(UpdateMeRequest.class);
            User user = updateMeUseCase.execute(email, request);
            ctx.json(mapToResponse(user));
        };
        this.deleteUser = ctx -> {
            try {
                var id = Integer.parseInt(ctx.pathParam("id"));
                this.deleteUserUseCase.execute(id);
                ctx.status(204);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Invalid ID format: ID must be a number");
            }
        };

    }


    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setGender(user.getGender());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setBirthdate(user.getBirthdate());
        response.setEmail(user.getEmail());
        return response;
    }
}
