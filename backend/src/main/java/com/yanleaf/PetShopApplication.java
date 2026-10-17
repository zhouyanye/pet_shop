package com.yanleaf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.yanleaf.mapper")
@EnableScheduling
public class PetShopApplication {

	public static void main (String[] args) {
		SpringApplication.run (PetShopApplication.class, args);
	}

}
