package com.lhjz.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lhjz.portal.entity.security.User;

@Configuration
public class BeanConfig {

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoderBean() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@ConfigurationProperties(prefix = "tms.super")
	User superUser() {
		return new User();
	}

}