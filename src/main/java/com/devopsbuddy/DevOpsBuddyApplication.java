package com.devopsbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class DevOpsBuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevOpsBuddyApplication.class, args);
	}

}
