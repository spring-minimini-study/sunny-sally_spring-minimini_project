package com.example.mini_spring.member.controller;

import com.example.mini_spring.common.response.ApiResponse;
import com.example.mini_spring.member.dto.MemberCreateRequest;
import com.example.mini_spring.member.dto.MemberResponse;
import com.example.mini_spring.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ApiResponse<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
        return ApiResponse.of(memberService.createMember(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long id) {
        return ApiResponse.of(memberService.getMember(id));
    }
}