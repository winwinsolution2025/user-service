package com.example.userservice.domain.usecase.impl;

import java.util.UUID;

import com.example.userservice.domain.entity.User;
import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.domain.exception.InvalidParameterException;

public class CreateUserUseCase implements com.example.userservice.domain.usecase.CreateUserUseCase {
    private final UserRepository repository;

    public CreateUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(CreateUserRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new InvalidParameterException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new InvalidParameterException("Email is required");
        }

        long numericId =  UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

        User user = new User();
        user.setUUID(numericId);
        user.setName(request.getName());
        user.setGender(request.getGender());
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setBirthdate(request.getBirthdate());
        user.setEmail(request.getEmail());
        
        repository.addUser(user);
        return user;
    }
} 