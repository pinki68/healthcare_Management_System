package com.healthcare.service;

import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		logger.debug("Attempting to load user by email: {}", email);

		Optional<User> userOptional = userRepository.findByEmail(email);

		if (!userOptional.isPresent()) {
			logger.error("User not found with email: {}", email);
			throw new UsernameNotFoundException("User not found with email: " + email);
		}

		User user = userOptional.get();
		logger.debug("User found: {}, Role: {}", user.getEmail(), user.getRole());

		Collection<? extends GrantedAuthority> authorities = getAuthorities(user.getRole());
		logger.debug("Granted authorities: {}", authorities);

		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
	}

	private Collection<? extends GrantedAuthority> getAuthorities(User.Role role) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		String roleString = "ROLE_" + role.name();
		logger.debug("Adding authority: {}", roleString);
		authorities.add(new SimpleGrantedAuthority(roleString));
		return authorities;
	}
}