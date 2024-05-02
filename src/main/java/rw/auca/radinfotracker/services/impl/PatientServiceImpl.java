package rw.auca.radinfotracker.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.repository.IPatientAuditRepository;
import rw.auca.radinfotracker.repository.IPatientRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.IPatientService;
import rw.auca.radinfotracker.utils.RandomUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements IPatientService {

    private final IPatientRepository patientRepository;

    private final IJwtService jwtService;

    private final IPatientAuditRepository patientAuditRepository;

    @Override
    public Patient register(NewPatientDTO dto) throws BadRequestException {

        LocalDate currentDate = LocalDate.now();
        LocalDate dateOfBirth = dto.getDateOfBirth();

        if (dateOfBirth.isAfter(currentDate)) {
            throw new BadRequestException("exceptions.badRequest.patient.invalidDate");
        }

        String refNumber = "PT-" + RandomUtil.randomNumber();

        while(patientRepository.findByRefNumber(refNumber).isPresent()){
            refNumber = "PT-" + RandomUtil.randomNumber();
        }

        Patient patient = new Patient(dto , refNumber);

        patient = patientRepository.save(patient);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        PatientAudit audit = new PatientAudit(patient, EAuditType.CREATE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "Patient registered", null);
        this.patientAuditRepository.save(audit);

        return patient;
    }

    @Override
    public Patient getById(UUID id) throws ResourceNotFoundException {
        return patientRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("exceptions.notFound.patient"));
    }

    @Override
    public Page<Patient> getAll(String query, EPatientStatus status, Pageable pageable) {
        return patientRepository.searchAll(query, status, pageable);
    }

    @Override
    public Patient deactivate(UUID id) throws ResourceNotFoundException, BadRequestException {
        Patient patient = getById(id);

        if(!patient.getStatus().equals(EPatientStatus.ACTIVE))
            throw new BadRequestException("exceptions.badRequest.patient.notActive");

        patient.setStatus(EPatientStatus.INACTIVE);

        patient = patientRepository.save(patient);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        PatientAudit audit = new PatientAudit(patient, EAuditType.DISABLE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(),"Patient status deactivated", null);
        this.patientAuditRepository.save(audit);

        return patient;
    }

    @Override
    public Patient activate(UUID id) throws ResourceNotFoundException, BadRequestException {
        Patient patient = getById(id);

        if(!patient.getStatus().equals(EPatientStatus.INACTIVE))
            throw new BadRequestException("exceptions.badRequest.patient.notInactive");

        patient.setStatus(EPatientStatus.ACTIVE);

        patient = patientRepository.save(patient);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        PatientAudit audit = new PatientAudit(patient, EAuditType.APPROVE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "Patient status activated", null);
        this.patientAuditRepository.save(audit);

        return patient;
    }

    @Override
    public Page<PatientAudit> getAuditByPatient(UUID id, Pageable pageable) throws ResourceNotFoundException {
        Patient patient = getById(id);

        return patientAuditRepository.findAllByPatient(patient, pageable);
    }
}
