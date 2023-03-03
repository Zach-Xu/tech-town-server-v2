package com.tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TechTownServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechTownServerApplication.class, args);
		System.out.println("Springboot started");
	}

}
