package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;

import java.util.List;

public interface ListUsersUseCase {
    List<User> execute(Integer[] ids);
}
