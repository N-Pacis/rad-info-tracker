package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.model.enums.EPaymentStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPatientAppointmentRepository extends JpaRepository<PatientAppointment, UUID> {

    @Query("SELECT pa FROM PatientAppointment pa WHERE " +
            "(:status IS NULL OR pa.status=:status) AND " +
            "(:paymentStatus IS NULL OR pa.paymentStatus=:paymentStatus) AND " +
            "(:date IS NULL or pa.date=:date) AND" +
            "(:technician IS NULL or pa.technician=:technician) AND" +
            "(:radiologist IS NULL or pa.radiologist=:radiologist)")
    Page<PatientAppointment> searchAllByDate(EAppointmentStatus status, EPaymentStatus paymentStatus, LocalDate date, UserAccount radiologist, UserAccount technician, Pageable pageable);

    Page<PatientAppointment> findAllByDateAndTechnicianAndStatus(LocalDate date, UserAccount technician, EAppointmentStatus status, Pageable pageable);

    Page<PatientAppointment> findAllByDateAndStatus(LocalDate date, EAppointmentStatus status, Pageable pageable);

    @Query("SELECT")
    Page<PatientAppointment> findAllByDateAndRadiologistAndStatus(LocalDate date, UserAccount radiologist, EAppointmentStatus status, Pageable pageable);

    Optional<PatientAppointment> findByRefNumber(String refNumber);
}
