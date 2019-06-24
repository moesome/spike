package com.moesome.spike.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.moesome.spike.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class SpringMVCConfig implements WebMvcConfigurer {
	@Autowired
	UserArgumentResolver userArgumentResolver;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("https://spike.moesome.com")
//				.allowedOrigins("http://spike.moesome.com")
				.allowedMethods("POST", "GET", "PUT", "PATCH","OPTIONS", "DELETE")
				.maxAge(3600)
				.allowCredentials(true);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(userArgumentResolver);
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource druidDataSource() {
		DruidDataSource druidDataSource = new DruidDataSource();
		return druidDataSource;
	}

}
