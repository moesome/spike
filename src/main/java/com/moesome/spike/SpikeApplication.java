package com.moesome.spike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableRetry
@EnableAsync
//@EnableTransactionManagement(mode = AdviceMode.ASPECTJ) 可以解决 springaop 失效
@EnableTransactionManagement
@EnableScheduling
@MapperScan("com.moesome.spike.model.dao")
public class SpikeApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpikeApplication.class, args);
	}
}
