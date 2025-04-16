package com.healthcare.controller;

import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.service.PatientService;
import com.healthcare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

	private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private PatientService patientService;

	@GetMapping
	public String viewProfile(Model model, Principal principal) {
		try {
			if (principal == null) {
				return "redirect:/login";
			}

			User user = userService.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));

			Optional<Patient> patientOpt = patientService.findByUser(user);
			Patient patient = patientOpt.orElse(new Patient());

			model.addAttribute("user", user);
			model.addAttribute("patient", patient);

			logger.info("Loaded profile for user: {}", user.getEmail());

			return "profile/view";
		} catch (Exception e) {
			logger.error("Error loading profile", e);
			model.addAttribute("error", "Error loading profile: " + e.getMessage());
			return "error";
		}
	}

	@GetMapping("/edit")
	public String showEditForm(Model model, Principal principal) {
		try {
			User user = userService.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));

			Optional<Patient> patientOpt = patientService.findByUser(user);
			Patient patient = patientOpt.orElse(new Patient());
			patient.setUser(user);

			model.addAttribute("user", user);
			model.addAttribute("patient", patient);

			return "profile/edit";
		} catch (Exception e) {
			logger.error("Error loading edit profile form", e);
			model.addAttribute("error", "Error loading profile data: " + e.getMessage());
			return "error";
		}
	}

	@PostMapping("/update")
	public String updateProfile(@ModelAttribute("user") User user, BindingResult userResult,
			@ModelAttribute("patient") Patient patient, BindingResult patientResult, Principal principal, Model model) {
		try {
			if (userResult.hasErrors() || patientResult.hasErrors()) {
				return "profile/edit";
			}

			User currentUser = userService.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));

			// Update user details but preserve ID and role
			user.setId(currentUser.getId());
			user.setRole(currentUser.getRole());
			user.setEmail(currentUser.getEmail()); // Don't allow email change for simplicity

			// Update user
			userService.updateUser(user);

			// Update or create patient profile
			Optional<Patient> existingPatient = patientService.findByUser(currentUser);
			if (existingPatient.isPresent()) {
				Patient updatedPatient = existingPatient.get();
				updatedPatient.setDateOfBirth(patient.getDateOfBirth());
				updatedPatient.setGender(patient.getGender());
				updatedPatient.setBloodGroup(patient.getBloodGroup());
				updatedPatient.setMedicalHistory(patient.getMedicalHistory());
				updatedPatient.setAllergies(patient.getAllergies());

				patientService.updatePatient(updatedPatient);
			} else {
				patient.setUser(currentUser);
				patientService.savePatient(patient);
			}

			logger.info("Updated profile for user: {}", currentUser.getEmail());

			return "redirect:/profile?updated";
		} catch (Exception e) {
			logger.error("Error updating profile", e);
			model.addAttribute("error", "Error updating profile: " + e.getMessage());
			return "profile/edit";
		}
	}
}