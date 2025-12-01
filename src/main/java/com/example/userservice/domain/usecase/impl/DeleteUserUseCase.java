package com.example.userservice.domain.usecase.impl;

import com.example.userservice.domain.exception.InvalidParameterException;
import com.example.userservice.domain.repository.UserRepository;

public class DeleteUserUseCase implements com.example.userservice.domain.usecase.DeleteUserUseCase {
    private final UserRepository repository;

    public DeleteUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidParameterException("Invalid user ID: ID must be a positive number");
        }

        repository.deleteUser(id);
    }
} 