package com.bsh.mailplatformmailservice;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableBatchProcessing
@EnableAsync
public class MailPlatFormMailServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MailPlatFormMailServiceApplication.class, args);
    }

}
