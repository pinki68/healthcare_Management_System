package com.healthcare.service;

import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private UserService userService;

	public List<Patient> findAllPatients() {
		return patientRepository.findAll();
	}

	public Patient findById(Long id) {
		return patientRepository.findById(id).orElseThrow(() -> new RuntimeException("Patient not found"));
	}

	public Optional<Patient> findByUser(User user) {
		return patientRepository.findByUser(user);
	}

	public Patient savePatient(Patient patient) {
		return patientRepository.save(patient);
	}

	public Patient updatePatient(Patient patient) {
		Patient existingPatient = findById(patient.getId());

		existingPatient.setDateOfBirth(patient.getDateOfBirth());
		existingPatient.setGender(patient.getGender());
		existingPatient.setBloodGroup(patient.getBloodGroup());
		existingPatient.setMedicalHistory(patient.getMedicalHistory());
		existingPatient.setAllergies(patient.getAllergies());

		return patientRepository.save(existingPatient);
	}

	public void deletePatient(Long id) {
		patientRepository.deleteById(id);
	}
}