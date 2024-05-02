package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.PatientAppointmentAudit;
import rw.auca.radinfotracker.model.PatientAudit;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPatientAppointmentAuditRepository extends JpaRepository<PatientAppointmentAudit, UUID> {
    Page<PatientAppointmentAudit> findAllByPatientAppointment(PatientAppointment patientAudit, Pageable pageable);
}
