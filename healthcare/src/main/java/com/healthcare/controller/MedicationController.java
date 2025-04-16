package com.healthcare.controller;

import com.healthcare.model.Medication;
import com.healthcare.model.User;
import com.healthcare.service.MedicationService;
import com.healthcare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/medications")
public class MedicationController {

	@Autowired
	private MedicationService medicationService;

	@Autowired
	private UserService userService;

	@GetMapping
	public String listMedications(Model model, Principal principal) {
		User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
		List<Medication> medications = medicationService.findByPatient(currentUser);
		model.addAttribute("medications", medications);
		return "medication/list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("medication", new Medication());
		return "medication/add";
	}

	@PostMapping("/add")
	public String addMedication(@Valid @ModelAttribute("medication") Medication medication, BindingResult result,
			Principal principal) {

		if (result.hasErrors()) {
			return "medication/add";
		}

		// Set the patient (current user)
		User patient = userService.findByEmail(principal.getName()).orElseThrow();
		medication.setPatient(patient);

		medicationService.saveMedication(medication);
		return "redirect:/medications?added";
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable("id") Long id, Model model, Principal principal) {
		Medication medication = medicationService.findById(id);

		// Security check - ensure the medication belongs to the current user
		User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
		if (!medication.getPatient().getId().equals(currentUser.getId())) {
			return "redirect:/medications?error";
		}

		model.addAttribute("medication", medication);
		return "medication/edit";
	}

	@PostMapping("/{id}/edit")
	public String updateMedication(@PathVariable("id") Long id,
			@Valid @ModelAttribute("medication") Medication medication, BindingResult result, Principal principal) {

		if (result.hasErrors()) {
			return "medication/edit";
		}

		// Security check - ensure the medication belongs to the current user
		Medication existingMedication = medicationService.findById(id);
		User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
		if (!existingMedication.getPatient().getId().equals(currentUser.getId())) {
			return "redirect:/medications?error";
		}

		medication.setId(id);
		medication.setPatient(currentUser);
		medicationService.updateMedication(medication);

		return "redirect:/medications?updated";
	}

	@GetMapping("/{id}/delete")
	public String deleteMedication(@PathVariable("id") Long id, Principal principal) {
		Medication medication = medicationService.findById(id);

		// Security check - ensure the medication belongs to the current user
		User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
		if (!medication.getPatient().getId().equals(currentUser.getId())) {
			return "redirect:/medications?error";
		}

		medicationService.deleteMedication(id);
		return "redirect:/medications?deleted";
	}
}