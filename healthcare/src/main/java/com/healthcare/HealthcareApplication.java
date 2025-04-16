package com.healthcare;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;

@SpringBootApplication
public class HealthcareApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthcareApplication.class, args);
	}

	@Bean
	public ApplicationRunner dataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Create a test user if none exists
			if (!userRepository.existsByEmail("test@example.com")) {
				User user = new User();
				user.setFirstName("Test");
				user.setLastName("User");
				user.setEmail("test@example.com");
				user.setPassword(passwordEncoder.encode("password"));
				user.setRole(User.Role.PATIENT);
				userRepository.save(user);
				System.out.println("Created test user: test@example.com / password");
			}
		};
	}

}
