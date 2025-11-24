package com.kernelLetter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class KernelLetterApplication {

	public static void main(String[] args) {
		SpringApplication.run(KernelLetterApplication.class, args);
	}

}
