package rw.auca.radinfotracker.repository;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.*;
import rw.auca.radinfotracker.utilities.Data;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@DataJpaTest
class IPatientAppointmentAuditRepositoryTest {

    @Autowired
    private IPatientAppointmentAuditRepository patientAppointmentAuditRepository;

    @Autowired
    private IPatientRepository patientRepository;

    @Autowired
    private IInsuranceRepository insuranceRepository;

    @Autowired
    private IImageTypeRepository imageTypeRepository;

    @Autowired
    private IUserRepository userAccountRepository;

    @Autowired
    private IPatientAppointmentRepository patientAppointmentRepository;

    @Autowired
    private FileRepository fileRepository;

    private final Faker faker = new Faker();

    @AfterEach
    void tearDown() {
        patientAppointmentAuditRepository.deleteAll();
        fileRepository.deleteAll();
        patientAppointmentRepository.deleteAll();
        patientRepository.deleteAll();
        insuranceRepository.deleteAll();
        imageTypeRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void itShouldReturnAllAuditsByAppointmentPaginated() {
        PatientAppointmentAudit audit = createPatientAppointmentAudit();

        Page<PatientAppointmentAudit> auditPage = patientAppointmentAuditRepository.findAllByPatientAppointment(audit.getPatientAppointment(), PageRequest.of(0, 10));

        assertEquals(auditPage.getTotalElements(), 1);
        assertThat(auditPage.getContent()).contains(audit);
    }

    private PatientAppointmentAudit createPatientAppointmentAudit(){
        return patientAppointmentAuditRepository.save(Data.createPatientAppointmentAudit());
    }

}