package com.moesome.spike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.moesome.spike.model.dao")
public class SpikeApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpikeApplication.class, args);
	}
}
