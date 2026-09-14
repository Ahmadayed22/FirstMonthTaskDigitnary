package com.todolist.service;

import java.util.List;
import java.util.Optional;

import com.todolist.entity.User;
import com.todolist.exception.UserNotFoundException;
import com.todolist.repo.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String name, String email) {
        return userRepository.save(new User(name, email, null));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User requireById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No user with id " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}