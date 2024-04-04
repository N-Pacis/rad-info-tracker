package rw.auca.radinfotracker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.util.List;
import java.util.UUID;

public interface IPatientService {

    Patient register(NewPatientDTO dto) throws BadRequestException;

    Patient getById(UUID id) throws ResourceNotFoundException;

    Page<Patient> getAll(String query, EPatientStatus status, Pageable pageable);

    Patient deactivate(UUID id) throws ResourceNotFoundException, BadRequestException;

    Patient activate(UUID id) throws ResourceNotFoundException, BadRequestException;

    List<PatientAudit> getAuditByPatient(UUID id) throws ResourceNotFoundException;
}
