package com.healthcare.repository;

import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByDoctorAndAppointmentDateTimeBetween(
            Doctor doctor, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientAndStatus(User patient, Appointment.Status status);
}