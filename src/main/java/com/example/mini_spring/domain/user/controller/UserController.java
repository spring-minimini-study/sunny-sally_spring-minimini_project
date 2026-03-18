package com.example.mini_spring.domain.user.controller;

import com.example.mini_spring.domain.user.dto.UserCreateRequest;
import com.example.mini_spring.domain.user.dto.UserResponse;
import com.example.mini_spring.domain.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/users")
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }
}