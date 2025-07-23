package com.flexswu.flexswu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FlexswuApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlexswuApplication.class, args);
	}

}
