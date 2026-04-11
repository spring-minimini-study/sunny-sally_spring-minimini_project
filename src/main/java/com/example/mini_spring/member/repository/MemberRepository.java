package com.example.mini_spring.member.repository;

import com.example.mini_spring.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // JPA가 자동으로 쿼리 만들어줄거임
    // 이메일 중복 체크
    boolean existsByEmail(String email);
}
