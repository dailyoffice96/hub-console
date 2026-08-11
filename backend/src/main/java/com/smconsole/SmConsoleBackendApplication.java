package com.smconsole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmConsoleBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmConsoleBackendApplication.class, args);
	}

}
