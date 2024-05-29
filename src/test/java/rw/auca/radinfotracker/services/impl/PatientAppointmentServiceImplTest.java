package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.dtos.NewPatientAppointmentDTO;
import rw.auca.radinfotracker.model.enums.*;
import rw.auca.radinfotracker.repository.IPatientAppointmentAuditRepository;
import rw.auca.radinfotracker.repository.IPatientAppointmentImageRepository;
import rw.auca.radinfotracker.repository.IPatientAppointmentRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.*;
import rw.auca.radinfotracker.utilities.Data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientAppointmentServiceImplTest {
    @Mock private IPatientAppointmentRepository patientAppointmentRepository;

    @Mock private IPatientAppointmentAuditRepository patientAppointmentAuditRepository;

    @Mock private IUserService userService;

    @Mock private IPatientService patientService;

    @Mock private IJwtService jwtService;

    @Mock private IInsuranceService insuranceService;

    @Mock private FileService fileService;

    @Mock private IPatientAppointmentImageRepository patientAppointmentImageRepository;

    @Mock private IImageTypeService imageTypeService;

    private PatientAppointmentServiceImpl patientAppointmentService;

    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        patientAppointmentService = new PatientAppointmentServiceImpl(patientAppointmentRepository, patientAppointmentAuditRepository, userService, patientService, jwtService, insuranceService, fileService, patientAppointmentImageRepository, imageTypeService);
    }

    @Test
    void create_shouldThrowBadRequestExceptionForInvalidRadiologist() throws ResourceNotFoundException {
        UserAccount technician = createTechnician();
        UserAccount radiologist = createRadiologist();
        Patient patient = Data.createPatient();

        NewPatientAppointmentDTO dto = new NewPatientAppointmentDTO();
        dto.setPatientId(UUID.randomUUID());
        dto.setRadiologistId(radiologist.getId());
        dto.setTechnicianId(technician.getId());
        dto.setInsuranceId(UUID.randomUUID());
        dto.setImageTypeId(UUID.randomUUID());
        dto.setDate(LocalDate.now().plusDays(1));

        when(patientService.getById(any(UUID.class))).thenReturn(patient);
        when(userService.getById(dto.getRadiologistId())).thenReturn(technician);

        assertThrows(BadRequestException.class, () -> patientAppointmentService.create(dto));
    }

    @Test
    void create_shouldThrowBadRequestExceptionForInvalidTechnician() throws ResourceNotFoundException {
        UserAccount technician = createTechnician();
        UserAccount radiologist = createRadiologist();
        Patient patient = Data.createPatient();

        NewPatientAppointmentDTO dto = new NewPatientAppointmentDTO();
        dto.setPatientId(UUID.randomUUID());
        dto.setRadiologistId(radiologist.getId());
        dto.setTechnicianId(technician.getId());
        dto.setInsuranceId(UUID.randomUUID());
        dto.setImageTypeId(UUID.randomUUID());
        dto.setDate(LocalDate.now().plusDays(1));

        when(patientService.getById(any(UUID.class))).thenReturn(patient);
        when(userService.getById(dto.getRadiologistId())).thenReturn(radiologist);
        when(userService.getById(dto.getTechnicianId())).thenReturn(radiologist);

        assertThrows(BadRequestException.class, () -> patientAppointmentService.create(dto));
    }

    @Test
    void create_shouldCreatePatientAppointment() throws ResourceNotFoundException, BadRequestException {
        Patient patient = Data.createPatient();
        UserAccount radiologist = createRadiologist();
        UserAccount technician = createTechnician();
        Insurance insurance = Data.createInsurance();
        ImageType imageType = Data.createImageType();

        NewPatientAppointmentDTO dto = new NewPatientAppointmentDTO();
        dto.setPatientId(patient.getId());
        dto.setRadiologistId(radiologist.getId());
        dto.setTechnicianId(technician.getId());
        dto.setInsuranceId(insurance.getId());
        dto.setImageTypeId(imageType.getId());
        dto.setDate(LocalDate.now().plusDays(1));


        CustomUserDTO userDTO = new CustomUserDTO(createRadiologist());
        PatientAppointment appointment = createPatientAppointment();

        when(patientService.getById(dto.getPatientId())).thenReturn(patient);
        when(userService.getById(dto.getRadiologistId())).thenReturn(radiologist);
        when(userService.getById(dto.getTechnicianId())).thenReturn(technician);
        when(insuranceService.getById(dto.getInsuranceId())).thenReturn(insurance);
        when(imageTypeService.getById(dto.getImageTypeId())).thenReturn(imageType);
        when(jwtService.extractLoggedInUser()).thenReturn(userDTO);
        when(patientAppointmentRepository.save(any(PatientAppointment.class))).thenReturn(appointment);

        patientAppointmentService.create(dto);

        verify(patientAppointmentRepository, times(1)).save(any(PatientAppointment.class));
        verify(patientAppointmentAuditRepository, times(1)).save(any(PatientAppointmentAudit.class));
    }

    private PatientAppointment createPatientAppointment(){
        Patient patient = Data.createPatient();

        Insurance insurance = Data.createInsurance();

        ImageType imageType = Data.createImageType();

        UserAccount radiologist = createRadiologist();

        UserAccount technician = createTechnician();

       return new PatientAppointment(UUID.randomUUID(), faker.code().asin(), LocalDate.now(), EAppointmentStatus.PENDING, patient, insurance, imageType, radiologist, technician, imageType.getTotalCost() * insurance.getRate());
    }

    private UserAccount createRadiologist(){
        return new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
    }

    private UserAccount createTechnician(){
        return new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.TECHNICIAN, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
    }
}