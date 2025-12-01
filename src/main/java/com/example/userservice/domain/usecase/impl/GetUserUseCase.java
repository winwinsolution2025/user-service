package com.example.userservice.domain.usecase.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.domain.exception.InvalidParameterException;

import java.util.Optional;

public class GetUserUseCase implements com.example.userservice.domain.usecase.GetUserUseCase {
    private final UserRepository repository;

    public GetUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> execute(Integer id) {
        if (id == null || id <= 0) {
            throw new InvalidParameterException("Invalid user ID: ID must be a positive number");
        }
        return repository.getUserById(id);
    }
}
