package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPatientAppointmentRepository extends JpaRepository<PatientAppointment, UUID> {

    @Query("SELECT pa FROM PatientAppointment pa WHERE " +
            "(:status IS NULL OR pa.status=:status) AND " +
            "(:date IS NULL or pa.date=:date) AND" +
            "(:technician IS NULL or pa.technician=:technician) AND" +
            "(:radiologist IS NULL or pa.radiologist=:radiologist)")
    Page<PatientAppointment> searchAllByDate(EAppointmentStatus status, LocalDate date, UserAccount radiologist, UserAccount technician, Pageable pageable);

    Page<PatientAppointment> findAllByDateAndTechnicianAndStatus(LocalDate date, UserAccount technician, EAppointmentStatus status, Pageable pageable);

    Page<PatientAppointment> findAllByDateAndRadiologistAndStatus(LocalDate date, UserAccount radiologist, EAppointmentStatus status, Pageable pageable);

    Optional<PatientAppointment> findByRefNumber(String refNumber);
}
