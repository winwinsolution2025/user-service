package com.example.userservice.domain.usecase;

import com.example.userservice.domain.entity.User;
import java.util.Optional;

public interface GetUserUseCase {
    Optional<User> execute(Integer id);
}
