package com.web.project.tp1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class Tp1Application {

	public static void main(String[] args) {
		SpringApplication.run(Tp1Application.class, args);
	}

}
