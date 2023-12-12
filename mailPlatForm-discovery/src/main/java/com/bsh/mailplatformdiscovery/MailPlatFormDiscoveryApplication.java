package com.bsh.mailplatformdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MailPlatFormDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailPlatFormDiscoveryApplication.class, args);
    }

}
