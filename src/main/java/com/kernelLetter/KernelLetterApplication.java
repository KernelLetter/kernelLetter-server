package com.kernelLetter;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
public class KernelLetterApplication {

	@PostConstruct
	public void init() {
		// 애플리케이션 전체의 기본 타임존을 한국 시간으로 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(KernelLetterApplication.class, args);
	}

}
