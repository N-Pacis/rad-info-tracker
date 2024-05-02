package rw.auca.radinfotracker.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.*;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IPatientAppointmentImageRepositoryTest {

    @Autowired
    private IPatientAppointmentImageRepository patientAppointmentImageRepository;

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

    @AfterEach
    void tearDown() {
        patientAppointmentImageRepository.deleteAll();
        fileRepository.deleteAll();
        patientAppointmentRepository.deleteAll();
        patientRepository.deleteAll();
        insuranceRepository.deleteAll();
        imageTypeRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void itShouldFindImagesByAppointmentPaginated() {
        Patient patient = new Patient(UUID.randomUUID(), "PT-3032", "Testing", "Firstname", "+25078943", LocalDate.now(), EPatientStatus.ACTIVE, "Testing address");
        patient = patientRepository.save(patient);

        Insurance insurance = new Insurance(UUID.randomUUID(), "Testing name", 0.8, EInsuranceStatus.ACTIVE);
        insurance = insuranceRepository.save(insurance);

        ImageType imageType = new ImageType(UUID.randomUUID(), "Testing name", EImageTypeStatus.ACTIVE, 10000.00);
        imageType = imageTypeRepository.save(imageType);

        UserAccount radiologist = new UserAccount(UUID.randomUUID(), "Testing name", "Last name", "testemail@gmail.com", "+2390232", ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, "93203wkfajkfa");
        radiologist = userAccountRepository.save(radiologist);

        UserAccount technician = new UserAccount(UUID.randomUUID(), "Testing name", "Last name", "testtechnicain@gmail.com", "+29010212", ERole.TECHNICIAN, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, "93203wkfajkfa");
        technician = userAccountRepository.save(technician);

        PatientAppointment appointment = new PatientAppointment(UUID.randomUUID(), "TR-00921", LocalDate.now(), EAppointmentStatus.PENDING, patient, insurance, imageType, radiologist, technician, 10000.00);
        appointment = patientAppointmentRepository.save(appointment);

        File file = new File(UUID.randomUUID(), "Testing image", "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
        file = fileRepository.save(file);

        PatientAppointmentImage image = new PatientAppointmentImage(UUID.randomUUID(),file,"Testing remarks", appointment);
        image = patientAppointmentImageRepository.save(image);

        Page<PatientAppointmentImage> imagesPage = patientAppointmentImageRepository.findByAppointment(appointment, PageRequest.of(0, 10));

        assertEquals(imagesPage.getTotalElements(), 1);
        assertThat(imagesPage.getContent()).contains(image);
    }

    @Test
    void itShouldReturnTheCountOfImagesByAppointment() {
        Patient patient = new Patient(UUID.randomUUID(), "PT-3032", "Testing", "Firstname", "+25078943", LocalDate.now(), EPatientStatus.ACTIVE, "Testing address");
        patient = patientRepository.save(patient);

        Insurance insurance = new Insurance(UUID.randomUUID(), "Testing name", 0.8, EInsuranceStatus.ACTIVE);
        insurance = insuranceRepository.save(insurance);

        ImageType imageType = new ImageType(UUID.randomUUID(), "Testing name", EImageTypeStatus.ACTIVE, 10000.00);
        imageType = imageTypeRepository.save(imageType);

        UserAccount radiologist = new UserAccount(UUID.randomUUID(), "Testing name", "Last name", "testemail@gmail.com", "+2390232", ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, "93203wkfajkfa");
        radiologist = userAccountRepository.save(radiologist);

        UserAccount technician = new UserAccount(UUID.randomUUID(), "Testing name", "Last name", "testtechnicain@gmail.com", "+29010212", ERole.TECHNICIAN, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, "93203wkfajkfa");
        technician = userAccountRepository.save(technician);

        PatientAppointment appointment = new PatientAppointment(UUID.randomUUID(), "TR-00921", LocalDate.now(), EAppointmentStatus.PENDING, patient, insurance, imageType, radiologist, technician, 10000.00);
        appointment = patientAppointmentRepository.save(appointment);

        File file = new File(UUID.randomUUID(), "Testing image", "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
        file = fileRepository.save(file);

        File secondFile = new File(UUID.randomUUID(), "Testing image 2", "", "", 0, EFileSizeType.B, "", EFileStatus.SAVED);
        secondFile = fileRepository.save(secondFile);

        PatientAppointmentImage image = new PatientAppointmentImage(UUID.randomUUID(),file,"Testing remarks", appointment);
        patientAppointmentImageRepository.save(image);

        PatientAppointmentImage secondImage = new PatientAppointmentImage(UUID.randomUUID(),secondFile,"Testing remarks", appointment);
        patientAppointmentImageRepository.save(secondImage);

        Integer expected = patientAppointmentImageRepository.countAllByAppointment(appointment);

        assertEquals(expected, 2);
    }
}