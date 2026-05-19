package com.example.food_ordering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class FoodOrderingApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(FoodOrderingApplication.class, args);
		Environment env = context.getEnvironment();
		String port = env.getProperty("server.port");
		if (port == null) {
			port = "8080";
		}
		System.out.println("\n----------------------------------------------------------");
		System.out.println("Application is running! Access it at:");
		System.out.println("Local: http://localhost:" + port);
		System.out.println("----------------------------------------------------------\n");
	}

}
