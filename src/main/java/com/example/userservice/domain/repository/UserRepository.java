package com.example.userservice.domain.repository;

import com.example.userservice.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void addUser(User user);

    Optional<User> getUserById(Integer id);

    Optional<User> getUserByEmail(String email);

    List<User> getAllUsers();

    List<User> getUsersByIds(Integer[] ids);

    boolean deleteUser(Integer id);

    boolean updateUser(User user);
}
