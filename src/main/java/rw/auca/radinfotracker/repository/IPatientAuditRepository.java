package rw.auca.radinfotracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.UserAccountAudit;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPatientAuditRepository extends JpaRepository<PatientAudit, UUID> {
    List<PatientAudit> findAllByPatient(Patient patientAudit);
}
