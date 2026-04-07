package com.example.mini_spring.member.controller;

import com.example.mini_spring.member.dto.MemberCreateRequest;
import com.example.mini_spring.member.dto.MemberResponse;
import com.example.mini_spring.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController // 이 클래스가 REST API 컨트롤러라는 뜻. 반환값을 JSON 형태로 내려줌
@RequestMapping("/members") // 공통 URL 경로 설정
public class MemberController {

    private final MemberService memberService;

    // 생성자 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원 생성
    @PostMapping // 요청 body의 JSON을 MemberCreateRequest로 받음
    public MemberResponse createMember(@RequestBody MemberCreateRequest request) {
        return memberService.createMember(request);
    }

    // 회원 단건 조회
    @GetMapping("/{id}") // URL 경로의 {id}값을 받아서 조히
    public MemberResponse getMember(@PathVariable Long id) {
        return memberService.getMember(id);
    }
}