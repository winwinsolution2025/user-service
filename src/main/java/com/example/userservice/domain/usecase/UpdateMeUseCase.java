package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;
import com.example.userservice.dto.UpdateMeRequest;

public interface UpdateMeUseCase {
    User execute(String email, UpdateMeRequest request);
}
