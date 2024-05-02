package rw.auca.radinfotracker.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.EFileSizeType;
import rw.auca.radinfotracker.model.enums.EFileStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IPatientAuditRepositoryTest {

    @Autowired
    private IPatientRepository patientRepository;

    @Autowired
    private IPatientAuditRepository patientAuditRepository;

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void tearDown() {
        patientAuditRepository.deleteAll();
        fileRepository.deleteAll();
        patientRepository.deleteAll();
    }

    @Test
    void itShouldReturnAuditsByPatient() {
        Patient patient = new Patient(UUID.randomUUID(), "PT-3032", "Testing", "Firstname", "+25078943", LocalDate.now(), EPatientStatus.ACTIVE, "Testing address");
        patient = patientRepository.save(patient);

        File file = new File(UUID.randomUUID(), "Testing name", "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
        file = fileRepository.save(file);

        PatientAudit audit = new PatientAudit(patient, EAuditType.CREATE, UUID.randomUUID(), "Testing name", "testingemail@test.test", "Testing remarks", file);
        audit = patientAuditRepository.save(audit);

        Page<PatientAudit> auditPage = patientAuditRepository.findAllByPatient(patient, PageRequest.of(0, 10));
        assertEquals(auditPage.getTotalElements(), 1);
        assertThat(auditPage.getContent()).contains(audit);
    }
}