package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;

public interface GetMeUseCase {
    User execute(String email);
}
