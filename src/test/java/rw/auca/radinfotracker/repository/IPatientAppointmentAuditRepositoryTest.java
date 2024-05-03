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
        Patient patient = new Patient(UUID.randomUUID(), faker.code().asin(), faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), LocalDate.now(), EPatientStatus.ACTIVE, faker.address().streetAddress());
        patient = patientRepository.save(patient);

        Insurance insurance = new Insurance(UUID.randomUUID(), faker.company().name(), faker.number().randomDouble(2, 0,1), EInsuranceStatus.ACTIVE);
        insurance = insuranceRepository.save(insurance);

        ImageType imageType = new ImageType(UUID.randomUUID(), faker.medical().medicineName(), EImageTypeStatus.ACTIVE, Double.valueOf(faker.commerce().price()));
        imageType = imageTypeRepository.save(imageType);

        UserAccount radiologist = new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
        radiologist = userAccountRepository.save(radiologist);

        UserAccount technician = new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
        technician = userAccountRepository.save(technician);

        PatientAppointment appointment = new PatientAppointment(UUID.randomUUID(), faker.code().asin(), LocalDate.now(), EAppointmentStatus.PENDING, patient, insurance, imageType, radiologist, technician, imageType.getTotalCost() * insurance.getRate());
        appointment =  patientAppointmentRepository.save(appointment);

        File file = new File(UUID.randomUUID(), faker.name().username(), "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
        file = fileRepository.save(file);

        PatientAppointmentAudit audit = new PatientAppointmentAudit(appointment, EAuditType.CREATE, UUID.randomUUID(), faker.name().fullName(), faker.internet().emailAddress(), "Testing remarks", file);

        return patientAppointmentAuditRepository.save(audit);
    }

}