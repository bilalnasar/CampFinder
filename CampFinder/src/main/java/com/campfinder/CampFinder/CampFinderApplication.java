package com.campfinder.CampFinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampFinderApplication.class, args);
	}

}
