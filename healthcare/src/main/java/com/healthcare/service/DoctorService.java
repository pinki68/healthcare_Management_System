package com.healthcare.service;

import com.healthcare.model.Doctor;
import com.healthcare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

	@Autowired
	private DoctorRepository doctorRepository;

	public List<Doctor> findAllDoctors() {
		return doctorRepository.findAll();
	}

	public Doctor findById(Long id) {
		return doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
	}

	public List<Doctor> findBySpecialization(String specialization) {
		return doctorRepository.findBySpecialization(specialization);
	}

	public Doctor saveDoctor(Doctor doctor) {
		return doctorRepository.save(doctor);
	}

	public void deleteDoctor(Long id) {
		doctorRepository.deleteById(id);
	}
}