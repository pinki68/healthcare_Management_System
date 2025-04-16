package com.healthcare.controller;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.healthcare.model.User;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.MedicationService;
import com.healthcare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private MedicationService medicationService;

	@GetMapping("/")
	public String home() {
		return "index";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model, Principal principal) {
		if (principal == null) {
			logger.warn("Principal is null in dashboard, redirecting to login");
			return "redirect:/login";
		}

		try {
			logger.debug("Loading dashboard for user: {}", principal.getName());
			User user = userService.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));

			// Add empty lists as placeholders to prevent null errors in the template
			model.addAttribute("upcomingAppointments", Collections.emptyList());
			model.addAttribute("medications", Collections.emptyList());

			// Make sure the authenticated user is available to the view
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				model.addAttribute("authenticatedUser", auth.getName());
			}

			return "dashboard";
		} catch (Exception e) {
			logger.error("Error loading dashboard", e);
			model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
			model.addAttribute("upcomingAppointments", Collections.emptyList());
			model.addAttribute("medications", Collections.emptyList());
			return "dashboard"; // Return dashboard anyway with the error message instead of error page
		}
	}
}