package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;
import com.example.userservice.dto.UpdateMeRequest;

import java.util.Optional;

public interface UpdateMeUseCase {
    Optional<User> execute(String email, UpdateMeRequest request);
}
