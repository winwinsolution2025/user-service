package com.example.userservice.domain.usecase.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.repository.UserRepository;

public class GetMeUseCase implements com.example.userservice.domain.usecase.GetMeUseCase {
    private final UserRepository repository;

    public GetMeUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(String email) {
        var user = repository.getUserByEmail(email);
        if (user.isEmpty()) {
            throw new NotFoundException("User", email);
        }

        return user.get();
    }
}
