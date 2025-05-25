package com.learnflow.learnflowserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LearnflowserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnflowserverApplication.class, args);
	}

}
