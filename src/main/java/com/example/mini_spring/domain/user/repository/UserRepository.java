package com.example.mini_spring.domain.user.repository;

import com.example.mini_spring.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}