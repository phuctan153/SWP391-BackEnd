package com.example.ev_rental_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
public class EvRentalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvRentalBackendApplication.class, args);
	}

}
