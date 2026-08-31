package com.example.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloHandler {

    @GetMapping("/hello")
    public String hello() {
        return "<h1>Hello Dreamers!</h1>";
    }
}
