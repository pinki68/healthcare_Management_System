package com.healthcare.service;

import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User registerUser(User user) {
		// Check if user already exists
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new RuntimeException("Email already in use");
		}

		// Encode password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Set default role if not specified
		if (user.getRole() == null) {
			user.setRole(User.Role.PATIENT);
		}

		return userRepository.save(user);
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	public User updateUser(User user) {
		User existingUser = findById(user.getId());

		// Update fields
		existingUser.setFirstName(user.getFirstName());
		existingUser.setLastName(user.getLastName());
		existingUser.setPhoneNumber(user.getPhoneNumber());

		// Don't update password if it's empty
		if (user.getPassword() != null && !user.getPassword().isEmpty()) {
			existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
		}

		return userRepository.save(existingUser);
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}
}