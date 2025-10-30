package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;
import com.example.userservice.dto.UpdateUserRequest;

public interface UpdateUserUseCase {
    User execute(Integer id, UpdateUserRequest request);
}
