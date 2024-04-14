package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.PatientAppointmentImage;

import java.util.UUID;

@Repository
public interface IPatientAppointmentImageRepository extends JpaRepository<PatientAppointmentImage, UUID> {

    Page<PatientAppointmentImage> findByAppointment(PatientAppointment patientAppointment, Pageable pageable);

    Integer countAllByAppointment(PatientAppointment patientAppointment);

}
