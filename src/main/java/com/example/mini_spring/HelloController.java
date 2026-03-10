package com.example.mini_spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스가 REST API 요청을 처리하는 컨트롤러임을 스프링에 알려줍니다.
public class HelloController {

    @GetMapping("/hello") // 브라우저에서 "/hello"라는 주소로 접속(GET 요청)하면 이 메서드가 실행됩니다.
    public String helloWorld() {
        return "Hello, World!";
    }
}