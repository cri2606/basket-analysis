package com.basket.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BasketAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasketAnalysisApplication.class, args);
	}

}
