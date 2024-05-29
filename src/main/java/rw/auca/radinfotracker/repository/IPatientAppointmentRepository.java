package rw.auca.radinfotracker.repository;

import org.springframework.cglib.core.Local;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPatientAppointmentRepository extends JpaRepository<PatientAppointment, UUID> {

    @Query("SELECT pa FROM PatientAppointment pa WHERE " +
            "(:status IS NULL OR pa.status = :status) AND " +
            "(:paymentStatus IS NULL OR pa.paymentStatus = :paymentStatus)" +
            "AND (CAST(:date AS  STRING)) IS NULL OR pa.date = :date")
    Page<PatientAppointment> searchAllByDate(EAppointmentStatus status, EPaymentStatus paymentStatus, LocalDate date, Pageable pageable);

    Page<PatientAppointment> findByStatusAndRadiologistAndDate(EAppointmentStatus status, UserAccount radiologist, LocalDate date, Pageable pageable);

    Page<PatientAppointment> findByStatusAndRadiologist(EAppointmentStatus status, UserAccount radiologist, Pageable pageable);

    Page<PatientAppointment> findByStatusAndTechnicianAndDate(EAppointmentStatus status, UserAccount technician, LocalDate date, Pageable pageable);
    Page<PatientAppointment> findByStatusAndTechnician(EAppointmentStatus status, UserAccount technician, Pageable pageable);

    Optional<PatientAppointment> findByRefNumber(String refNumber);
}
