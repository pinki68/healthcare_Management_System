package com.healthcare.controller;

import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

	private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private UserService userService;

	// Add this to make appointment status enum available to all templates
	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("AppointmentStatus", Appointment.Status.class);
	}

	@GetMapping
	public String listAppointments(Model model, Principal principal) {
		try {
			User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
			List<Appointment> appointments = appointmentService.findByPatient(currentUser);

			// Make sure the list is not null
			if (appointments == null) {
				appointments = new ArrayList<>();
			}

			model.addAttribute("appointments", appointments);
			return "appointment/list";
		} catch (Exception e) {
			// Handle any errors gracefully
			model.addAttribute("error", "Error loading appointments: " + e.getMessage());
			model.addAttribute("appointments", new ArrayList<>());
			return "appointment/list";
		}
	}

	@GetMapping("/book")
	public String showBookingForm(Model model) {
		try {
			Appointment appointment = new Appointment();
			appointment.setStatus(Appointment.Status.SCHEDULED); // Initialize with a status
			model.addAttribute("appointment", appointment);

			List<Doctor> doctors = doctorService.findAllDoctors();
			logger.info("Found {} doctors", doctors.size());

			// If no doctors exist, create a sample doctor for testing
			if (doctors.isEmpty()) {
				logger.warn("No doctors found in database, creating a sample doctor");

				Doctor sampleDoctor = new Doctor();
				sampleDoctor.setFirstName("John");
				sampleDoctor.setLastName("Smith");
				sampleDoctor.setSpecialization("General Medicine");
				sampleDoctor.setQualification("MD");
				sampleDoctor.setExperience("10 years");

				sampleDoctor = doctorService.saveDoctor(sampleDoctor);
				doctors.add(sampleDoctor);

				logger.info("Created sample doctor: {} {}", sampleDoctor.getFirstName(), sampleDoctor.getLastName());
			}

			// Debug output for each doctor
			for (Doctor doctor : doctors) {
				logger.info("Doctor: {} {} ({})", doctor.getFirstName(), doctor.getLastName(),
						doctor.getSpecialization());
			}

			model.addAttribute("doctors", doctors);
			return "appointment/book";

		} catch (Exception e) {
			logger.error("Error loading booking form", e);
			model.addAttribute("error", "Error loading doctors: " + e.getMessage());
			model.addAttribute("appointment", new Appointment());
			model.addAttribute("doctors", new ArrayList<>());
			return "appointment/book";
		}
	}

	@PostMapping("/book")
	public String bookAppointment(@Valid @ModelAttribute("appointment") Appointment appointment, BindingResult result,
			@RequestParam("doctorId") Long doctorId,
			@RequestParam("appointmentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
			@RequestParam("appointmentTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime appointmentTime,
			Principal principal, Model model) {

		if (result.hasErrors()) {
			model.addAttribute("doctors", doctorService.findAllDoctors());
			return "appointment/book";
		}

		try {
			logger.info("Booking appointment with doctorId: {}", doctorId);

			// Set the appointment date and time
			LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
			appointment.setAppointmentDateTime(appointmentDateTime);

			// Set the doctor
			Doctor doctor = doctorService.findById(doctorId);
			logger.info("Found doctor: {} {}", doctor.getFirstName(), doctor.getLastName());
			appointment.setDoctor(doctor);

			// Set the patient (current user)
			User patient = userService.findByEmail(principal.getName()).orElseThrow();
			appointment.setPatient(patient);

			// Ensure status is set
			if (appointment.getStatus() == null) {
				appointment.setStatus(Appointment.Status.SCHEDULED);
			}

			// Book the appointment
			appointmentService.bookAppointment(appointment);

			return "redirect:/appointments?booked";
		} catch (Exception e) {
			logger.error("Error booking appointment", e);
			model.addAttribute("error", "Error booking appointment: " + e.getMessage());
			model.addAttribute("doctors", doctorService.findAllDoctors());
			return "appointment/book";
		}
	}

	@GetMapping("/{id}")
	public String viewAppointment(@PathVariable("id") Long id, Model model, Principal principal) {
		try {
			Appointment appointment = appointmentService.findById(id);

			// Security check - ensure the appointment belongs to the current user
			User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
			if (!appointment.getPatient().getId().equals(currentUser.getId())) {
				return "redirect:/appointments?error";
			}

			model.addAttribute("appointment", appointment);
			return "appointment/view";
		} catch (Exception e) {
			return "redirect:/appointments?error";
		}
	}

	@GetMapping("/{id}/cancel")
	public String cancelAppointment(@PathVariable("id") Long id, Principal principal) {
		try {
			Appointment appointment = appointmentService.findById(id);

			// Security check - ensure the appointment belongs to the current user
			User currentUser = userService.findByEmail(principal.getName()).orElseThrow();
			if (!appointment.getPatient().getId().equals(currentUser.getId())) {
				return "redirect:/appointments?error";
			}

			appointmentService.cancelAppointment(id);
			return "redirect:/appointments?cancelled";
		} catch (Exception e) {
			return "redirect:/appointments?error";
		}
	}
}