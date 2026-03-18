package com.example.mini_spring.domain.user.service;

import com.example.mini_spring.domain.user.domain.User;
import com.example.mini_spring.domain.user.dto.UserCreateRequest;
import com.example.mini_spring.domain.user.dto.UserResponse;
import com.example.mini_spring.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));

        return new UserResponse(user.getId(), user.getName());
    }

    public UserResponse createUser(UserCreateRequest request) {
        User user = new User(request.getName());
        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getName());
    }
}