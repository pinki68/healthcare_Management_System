package com.healthcare.controller;

import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.service.PatientService;
import com.healthcare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private PatientService patientService;

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("patient", new Patient());
		return "auth/register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult userResult,
			@ModelAttribute("patient") Patient patient, BindingResult patientResult, Model model) {

		if (userResult.hasErrors()) {
			return "auth/register";
		}

		try {
			// Register the user
			user.setRole(User.Role.PATIENT);
			User savedUser = userService.registerUser(user);

			// Create patient profile
			patient.setUser(savedUser);
			patientService.savePatient(patient);

			return "redirect:/login?registered";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "auth/register";
		}
	}
}