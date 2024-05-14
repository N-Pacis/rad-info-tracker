package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.repository.IPatientAuditRepository;
import rw.auca.radinfotracker.repository.IPatientRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.utilities.Data;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock private IPatientRepository patientRepository;

    @Mock private IJwtService jwtService;

    @Mock private IPatientAuditRepository patientAuditRepository;

    private PatientServiceImpl patientService;

    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        patientService = new PatientServiceImpl(patientRepository, jwtService, patientAuditRepository);
    }

    @Test
    void register_shouldThrowBadRequestExceptionForFutureDateOfBirth() {
        NewPatientDTO dto = new NewPatientDTO(faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), faker.address().streetAddress(), LocalDate.now().plusDays(1));

        assertThrows(BadRequestException.class, () -> patientService.register(dto));
    }

    @Test
    void register_shouldRegisterNewPatient() throws BadRequestException {
        NewPatientDTO dto = new NewPatientDTO(faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(),faker.address().streetAddress(), LocalDate.now().minusDays(1));
        CustomUserDTO userDTO = new CustomUserDTO(Data.createTechnician());
        when(jwtService.extractLoggedInUser()).thenReturn(userDTO);
        when(patientRepository.save(any(Patient.class))).thenReturn(Data.createPatient());

        patientService.register(dto);

        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(patientAuditRepository, times(1)).save(any(PatientAudit.class));
    }

    @Test
    void deactivate_shouldThrowBadRequestExceptionForInactivePatient() {
        Patient patient = Data.createPatient();
        patient.setStatus(EPatientStatus.INACTIVE);
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));

        assertThrows(BadRequestException.class, () -> patientService.deactivate(patient.getId()));
    }

    @Test
    void deactivate_shouldDeactivatePatient() throws BadRequestException, ResourceNotFoundException {
        Patient patient = Data.createPatient();
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        CustomUserDTO userDTO = new CustomUserDTO(Data.createTechnician());
        when(jwtService.extractLoggedInUser()).thenReturn(userDTO);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        patientService.deactivate(patient.getId());

        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(patientAuditRepository, times(1)).save(any(PatientAudit.class));
    }

    @Test
    void activate_shouldThrowBadRequestExceptionForActivePatient() {
        Patient patient = Data.createPatient();
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));

        assertThrows(BadRequestException.class, () -> patientService.activate(patient.getId()));
    }

    @Test
    void activate_shouldActivatePatient() throws BadRequestException, ResourceNotFoundException {
        Patient patient = Data.createPatient();
        patient.setStatus(EPatientStatus.INACTIVE);
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        CustomUserDTO userDTO = new CustomUserDTO(Data.createTechnician());
        when(jwtService.extractLoggedInUser()).thenReturn(userDTO);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        patientService.activate(patient.getId());

        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(patientAuditRepository, times(1)).save(any(PatientAudit.class));
    }
    
}