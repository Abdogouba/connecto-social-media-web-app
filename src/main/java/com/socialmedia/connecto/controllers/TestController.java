package com.socialmedia.connecto.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supers")
public class TestController {

    @GetMapping
    public ResponseEntity test() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello Connecto!");
    }

}
