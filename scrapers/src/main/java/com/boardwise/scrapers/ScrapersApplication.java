package com.boardwise.scrapers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling 
@SpringBootApplication
public class ScrapersApplication {
 
	public static void main(String[] args) {
		SpringApplication.run(ScrapersApplication.class, args);
		
	}

}
