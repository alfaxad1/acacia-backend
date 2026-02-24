package com.example.acacia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AcaciaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcaciaApplication.class, args);
	}

}
