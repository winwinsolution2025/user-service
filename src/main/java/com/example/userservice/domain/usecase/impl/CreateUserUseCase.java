package com.example.userservice.domain.usecase.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.dto.CreateUserRequest;

import java.util.UUID;

public class CreateUserUseCase implements com.example.userservice.domain.usecase.CreateUserUseCase {
    private final UserRepository repository;

    public CreateUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(CreateUserRequest request) {
        long numericId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

        User user = new User();
        user.setUUID(numericId);
        user.setName(request.getName());
        user.setGender(request.getGender());
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        if (request.getAvatarId() == null || request.getAvatarFrameId() == null) {
            request.setAvatarId(0);
            request.setAvatarFrameId(0);
        }
        user.setAvatarId(request.getAvatarId());
        user.setAvatarFrameId(request.getAvatarFrameId());
        user.setBirthdate(request.getBirthdate());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        repository.addUser(user);
        return user;
    }
} 