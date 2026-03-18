package com.example.mini_spring.domain.user.dto;

public class UserResponse {

    private Long id;
    private String name;

    public UserResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}