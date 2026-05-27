package com.example.userservice.domain.usecase.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.repository.UserRepository;

import java.util.List;

public class ListUsersUseCase implements com.example.userservice.domain.usecase.ListUsersUseCase {
    private final UserRepository repository;

    public ListUsersUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<User> execute(Integer[] ids) {
        if (ids == null) {
            return repository.getAllUsers();
        }

        return repository.getUsersByIds(ids);
    }
} 