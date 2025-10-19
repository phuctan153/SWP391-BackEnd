package com.example.ev_rental_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvRentalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvRentalBackendApplication.class, args);
	}

}
