package rw.auca.radinfotracker.repository;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IPatientAppointmentRepositoryTest {

    @Autowired
    private IPatientAppointmentRepository patientAppointmentRepository;

    @Autowired
    private IPatientRepository patientRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IInsuranceRepository insuranceRepository;

    @Autowired
    private IImageTypeRepository imageTypeRepository;

    private final Faker faker = new Faker();

    @AfterEach
    void tearDown() {
        patientAppointmentRepository.deleteAll();
        patientRepository.deleteAll();
        insuranceRepository.deleteAll();
        imageTypeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnAppointmentsByAndDate() {
        PatientAppointment appointment = createPatientAppointment();
        LocalDate searchDate = appointment.getDate();

        Page<PatientAppointment> appointmentPage = patientAppointmentRepository.searchAllByDate(null, null, searchDate, null, null, PageRequest.of(0, 10));

        assertEquals(appointmentPage.getTotalElements(), 1);
        assertThat(appointmentPage.getContent()).contains(appointment);
        assertEquals(appointmentPage.getContent().get(0).getDate(), searchDate);
    }

    @Test
    void shouldReturnAppointmentByRefNumber() {
        PatientAppointment appointment = createPatientAppointment();

        Optional<PatientAppointment> appointmentOptional = patientAppointmentRepository.findByRefNumber(appointment.getRefNumber());

        assertTrue(appointmentOptional.isPresent());
        assertEquals(appointmentOptional.get().getRefNumber(),appointment.getRefNumber());
    }

    @Test
    void shouldReturnNullWhenAppointmentByRefNumberDoesNotExist() {

        String refNumber = faker.code().gtin13();

        Optional<PatientAppointment> appointmentOptional = patientAppointmentRepository.findByRefNumber(refNumber);

        assertTrue(appointmentOptional.isEmpty());
    }

    private PatientAppointment createPatientAppointment(){
        Patient patient = new Patient(UUID.randomUUID(), faker.code().asin(), faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), LocalDate.now(), EPatientStatus.ACTIVE, faker.address().streetAddress());
        patient = patientRepository.save(patient);

        Insurance insurance = new Insurance(UUID.randomUUID(), faker.company().name(), faker.number().randomDouble(2, 0,1), EInsuranceStatus.ACTIVE);
        insurance = insuranceRepository.save(insurance);

        ImageType imageType = new ImageType(UUID.randomUUID(), faker.medical().medicineName(), EImageTypeStatus.ACTIVE, Double.valueOf(faker.commerce().price()));
        imageType = imageTypeRepository.save(imageType);

        UserAccount radiologist = new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
        radiologist = userRepository.save(radiologist);

        UserAccount technician = new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
        technician = userRepository.save(technician);

        PatientAppointment appointment = new PatientAppointment(UUID.randomUUID(), faker.code().asin(), LocalDate.now(), EAppointmentStatus.PENDING, patient, insurance, imageType, radiologist, technician, imageType.getTotalCost() * insurance.getRate());
        return patientAppointmentRepository.save(appointment);
    }
}