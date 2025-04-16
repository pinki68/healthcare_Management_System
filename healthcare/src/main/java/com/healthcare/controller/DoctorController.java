package com.healthcare.controller;

import com.healthcare.model.Doctor;
import com.healthcare.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

	private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

	@Autowired
	private DoctorService doctorService;

	@GetMapping
	public String listDoctors(Model model) {
		List<Doctor> doctors = doctorService.findAllDoctors();
		model.addAttribute("doctors", doctors);

		// Check if there are any doctors
		if (doctors.isEmpty()) {
			model.addAttribute("warning", "No doctors found in the system. Add a doctor below.");
		}

		return "doctor/list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("doctor", new Doctor());
		return "doctor/add";
	}

	@PostMapping("/add")
	public String addDoctor(Doctor doctor) {
		try {
			logger.info("Adding doctor: {} {}", doctor.getFirstName(), doctor.getLastName());
			doctorService.saveDoctor(doctor);
			return "redirect:/doctors?added";
		} catch (Exception e) {
			logger.error("Error adding doctor", e);
			return "redirect:/doctors?error";
		}
	}
}
