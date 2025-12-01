package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;
import com.example.userservice.dto.UpdateUserRequest;
import java.util.Optional;

public interface UpdateUserUseCase {
    Optional<User> execute(Integer id, UpdateUserRequest request);
}
