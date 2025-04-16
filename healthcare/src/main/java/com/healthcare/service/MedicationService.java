package com.healthcare.service;

import com.healthcare.model.Medication;
import com.healthcare.model.User;
import com.healthcare.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {

	@Autowired
	private MedicationRepository medicationRepository;

	public List<Medication> findAllMedications() {
		return medicationRepository.findAll();
	}

	public Medication findById(Long id) {
		return medicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Medication not found"));
	}

	public List<Medication> findByPatient(User patient) {
		return medicationRepository.findByPatient(patient);
	}

	public Medication saveMedication(Medication medication) {
		return medicationRepository.save(medication);
	}

	public Medication updateMedication(Medication medication) {
		Medication existingMedication = findById(medication.getId());

		existingMedication.setName(medication.getName());
		existingMedication.setDosage(medication.getDosage());
		existingMedication.setFrequency(medication.getFrequency());
		existingMedication.setStartDate(medication.getStartDate());
		existingMedication.setEndDate(medication.getEndDate());
		existingMedication.setInstructions(medication.getInstructions());

		return medicationRepository.save(existingMedication);
	}

	public void deleteMedication(Long id) {
		medicationRepository.deleteById(id);
	}
}