package com.example.xpenso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class XpensoApplication {

	public static void main(String[] args) {
		SpringApplication.run(XpensoApplication.class, args);
	}

}
