package com.example.mini_spring.member.repository;

import com.example.mini_spring.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Member, Long>
// - Member: 어떤 엔티티를 다룰지
// - Long: 그 엔티티의 PK 타입이 무엇인지
// 이것만 작성해도 기본적으로 save(), findById(), findAll(), deleteById()들을 바로 사용할 수 있음
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
}
