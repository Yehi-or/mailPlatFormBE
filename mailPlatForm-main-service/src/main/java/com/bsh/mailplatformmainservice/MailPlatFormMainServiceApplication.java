package com.bsh.mailplatformmainservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.Async;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@Async
public class MailPlatFormMainServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailPlatFormMainServiceApplication.class, args);
	}

}
