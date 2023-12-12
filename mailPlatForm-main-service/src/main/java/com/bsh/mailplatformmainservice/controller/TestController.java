package com.bsh.mailplatformmainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/main-service")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final Environment env;

    @GetMapping("/check")
    public String check(HttpServletRequest request) {
        log.info("Server port : {}", request.getServerPort());
        return String.format("this is main-service port number : %s", env.getProperty("local.server.port"));
    }

}
