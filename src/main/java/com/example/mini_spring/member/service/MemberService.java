package com.example.mini_spring.member.service;

import com.example.mini_spring.member.domain.Member;
import com.example.mini_spring.member.dto.MemberCreateRequest;
import com.example.mini_spring.member.dto.MemberResponse;
import com.example.mini_spring.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

// 이걸 붙이면 스프링이 이 클래스를 Bean으로 등록하고 관리 대상으로 인식함
// 객체를 직접 new MemberServie() 하지 않아도 되고 다른 곳에서 주입받아서 쓸 수 있음
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원 생성
    public MemberResponse createMember(MemberCreateRequest request) {
        if(memberRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }
        Member member = new Member(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        Member savedMember = memberRepository.save(member);

        return new MemberResponse(
                savedMember.getId(),
                savedMember.getName(),
                savedMember.getEmail()
        );
    }

    // 회원 단건 조회
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. id=" + memberId));

        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
