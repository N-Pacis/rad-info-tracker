package rw.auca.radinfotracker.repository;

import com.github.javafaker.Faker;
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
class IPatientRepositoryTest {

    @Autowired
    private IPatientRepository patientRepository;

    private final Faker faker = new Faker();


    @Test
    void shouldReturnAllPatients() {
        Patient patient = createPatient();

        Page<Patient> patientPage = patientRepository.searchAll("", null, PageRequest.of(0, 10));

        assertEquals(patientPage.getTotalElements(), 1);
        assertThat(patientPage.getContent()).contains(patient);
    }

    @Test
    void shouldReturnAllPatientsByGivenFilters() {
        Patient patient = createPatient();
        Patient secondPatient = createPatient();

        Page<Patient> patientPage = patientRepository.searchAll(secondPatient.getPhoneNumber(), secondPatient.getStatus(), PageRequest.of(0, 10));

        assertEquals(patientPage.getTotalElements(), 1);
        assertThat(patientPage.getContent()).contains(secondPatient);
    }

    @Test
    void shouldFindPatientByRefNumber() {
        Patient patient = createPatient();

        Optional<Patient> optionalPatient = patientRepository.findByRefNumber(patient.getRefNumber());

        assertTrue(optionalPatient.isPresent());
        assertEquals(optionalPatient.get().getRefNumber(), patient.getRefNumber());
    }

    @Test
    void shouldReturnNullWhenRefNumberCannotBeFound() {
        String refNumber = faker.code().gtin13();

        Optional<Patient> optionalPatient = patientRepository.findByRefNumber(refNumber);

        assertTrue(optionalPatient.isEmpty());
    }

    private Patient createPatient(){
        Patient patient = new Patient(UUID.randomUUID(), faker.code().asin(), faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), LocalDate.now(), EPatientStatus.ACTIVE, faker.address().streetAddress());
        return patientRepository.save(patient);
    }
}