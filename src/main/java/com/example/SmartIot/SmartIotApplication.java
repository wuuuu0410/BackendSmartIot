package com.example.SmartIot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SmartIotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartIotApplication.class, args);
	}

}
