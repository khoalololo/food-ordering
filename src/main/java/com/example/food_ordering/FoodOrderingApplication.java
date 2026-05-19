package com.example.food_ordering;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.enums.Role;
import com.example.food_ordering.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

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

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("manager@food.com")) {
				User manager = User.builder()
						.fullName("Default Manager")
						.email("manager@food.com")
						.password(passwordEncoder.encode("admin123"))
						.phone("0123456789")
						.role(Role.MANAGER)
						.build();
				userRepository.save(manager);
				System.out.println("[INIT] Created default manager: manager@food.com / admin123");
			}
		};
	}

}
