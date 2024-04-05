package rw.auca.radinfotracker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.util.List;
import java.util.UUID;

public interface IInsuranceService {

    Insurance register(NewInsuranceDTO dto) throws BadRequestException;

    Insurance getById(UUID id) throws ResourceNotFoundException;

    Page<Insurance> getAll(String query, EInsuranceStatus status, Pageable pageable);

    List<Insurance> getAll(String query);
}
