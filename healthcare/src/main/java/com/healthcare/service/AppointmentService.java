package com.healthcare.service;

import com.healthcare.model.Appointment;

import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private DoctorRepository doctorRepository;

	public List<Appointment> findAllAppointments() {
		return appointmentRepository.findAll();
	}

	public Appointment findById(Long id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		// Ensure doctor is loaded
		if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
			Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
			appointment.setDoctor(doctor);
		}

		return appointment;
	}

	public List<Appointment> findByPatient(User patient) {
		List<Appointment> appointments = appointmentRepository.findByPatient(patient);
		List<Appointment> enhancedAppointments = new ArrayList<>();

		for (Appointment appointment : appointments) {
			// Ensure doctor is loaded
			if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
				Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
				appointment.setDoctor(doctor);
			}
			enhancedAppointments.add(appointment);
		}

		return enhancedAppointments;
	}

	public List<Appointment> findByDoctor(Doctor doctor) {
		return appointmentRepository.findByDoctor(doctor);
	}

	public List<Appointment> findByDoctorAndDateRange(Doctor doctor, LocalDateTime start, LocalDateTime end) {
		return appointmentRepository.findByDoctorAndAppointmentDateTimeBetween(doctor, start, end);
	}

	public List<Appointment> findByPatientAndStatus(User patient, Appointment.Status status) {
		List<Appointment> appointments = appointmentRepository.findByPatientAndStatus(patient, status);
		List<Appointment> enhancedAppointments = new ArrayList<>();

		for (Appointment appointment : appointments) {
			// Ensure doctor is loaded
			if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
				Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
				appointment.setDoctor(doctor);
			}
			enhancedAppointments.add(appointment);
		}

		return enhancedAppointments;
	}

	public Appointment bookAppointment(Appointment appointment) {
		// Check if the time slot is available
		Doctor doctor = appointment.getDoctor();
		LocalDateTime appointmentTime = appointment.getAppointmentDateTime();

		// Get all appointments for the doctor on the same day
		LocalDateTime dayStart = appointmentTime.toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = dayStart.plusDays(1);

		List<Appointment> existingAppointments = findByDoctorAndDateRange(doctor, dayStart, dayEnd);

		// Check for conflicts (assuming appointments are 1 hour long)
		boolean hasConflict = existingAppointments.stream().anyMatch(existing -> {
			LocalDateTime existingStart = existing.getAppointmentDateTime();
			LocalDateTime existingEnd = existingStart.plusHours(1);
			LocalDateTime newStart = appointment.getAppointmentDateTime();
			LocalDateTime newEnd = newStart.plusHours(1);

			return (newStart.isEqual(existingStart) || newStart.isAfter(existingStart))
					&& newStart.isBefore(existingEnd)
					|| (newEnd.isAfter(existingStart) && newEnd.isBefore(existingEnd) || newEnd.isEqual(existingEnd));
		});

		if (hasConflict) {
			throw new RuntimeException("The selected time slot is not available");
		}

		// Set initial status
		appointment.setStatus(Appointment.Status.SCHEDULED);

		// Make sure we have the full doctor object
		if (doctor != null && doctor.getId() != null) {
			Doctor fullDoctor = doctorRepository.findById(doctor.getId())
					.orElseThrow(() -> new RuntimeException("Doctor not found"));
			appointment.setDoctor(fullDoctor);
		}

		return appointmentRepository.save(appointment);
	}

	public Appointment updateAppointment(Appointment appointment) {
		Appointment existingAppointment = findById(appointment.getId());

		existingAppointment.setAppointmentDateTime(appointment.getAppointmentDateTime());
		existingAppointment.setStatus(appointment.getStatus());
		existingAppointment.setReason(appointment.getReason());
		existingAppointment.setNotes(appointment.getNotes());

		// Make sure the doctor is properly set
		if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
			Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId())
					.orElseThrow(() -> new RuntimeException("Doctor not found"));
			existingAppointment.setDoctor(doctor);
		}

		return appointmentRepository.save(existingAppointment);
	}

	public Appointment cancelAppointment(Long id) {
		Appointment appointment = findById(id);
		appointment.setStatus(Appointment.Status.CANCELLED);
		return appointmentRepository.save(appointment);
	}

	public void deleteAppointment(Long id) {
		appointmentRepository.deleteById(id);
	}
}