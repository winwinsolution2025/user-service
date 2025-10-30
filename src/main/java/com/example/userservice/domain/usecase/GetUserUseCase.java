package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;

public interface GetUserUseCase {
    User execute(Integer id);
}
