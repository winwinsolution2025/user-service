package com.example.userservice.domain.usecase.impl;

import com.example.userservice.domain.entity.User;
import com.example.userservice.domain.exception.NotFoundException;
import com.example.userservice.domain.repository.UserRepository;
import com.example.userservice.dto.UpdateMeRequest;

import java.util.Optional;

public class UpdateMeUseCase implements com.example.userservice.domain.usecase.UpdateMeUseCase {
    private final UserRepository repository;

    public UpdateMeUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> execute(String email, UpdateMeRequest request) {

        // First get the existing user
        Optional<User> existingUserOpt = repository.getUserByEmail(email);
        if (existingUserOpt.isEmpty()) {
            throw new NotFoundException("User", email);
        }

        var user = existingUserOpt.get();
        updateExistingUser(request, user);
        repository.updateUser(existingUserOpt.get());
        return existingUserOpt;
    }

    private void updateExistingUser(UpdateMeRequest request, User existingUser) {
        // Update only the fields that are provided
        if (request.getName() != null) {
            existingUser.setName(request.getName());
        }
        if (request.getGender() != null) {
            existingUser.setGender(request.getGender());
        }
        if (request.getNickname() != null) {
            existingUser.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            existingUser.setAvatar(request.getAvatar());
        }
        if (request.getBirthdate() != null) {
            existingUser.setBirthdate(request.getBirthdate());
        }
    }
}