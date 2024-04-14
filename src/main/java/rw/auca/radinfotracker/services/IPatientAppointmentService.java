package rw.auca.radinfotracker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.PatientAppointmentAudit;
import rw.auca.radinfotracker.model.PatientAppointmentImage;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.dtos.NewPatientAppointmentDTO;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IPatientAppointmentService {
     PatientAppointment create(NewPatientAppointmentDTO dto) throws ResourceNotFoundException, BadRequestException;

     Page<PatientAppointment> searchAllByDate(EAppointmentStatus status, LocalDate date, UUID radiologist, UUID technician, Pageable pageable) throws ResourceNotFoundException, BadRequestException;

     Page<PatientAppointment> getAllMyAppointmentsByDate(LocalDate date, Pageable pageable) throws ResourceNotFoundException;

     PatientAppointment getById(UUID id) throws BadRequestException, ResourceNotFoundException;

     List<PatientAppointmentAudit> getAppointmentAudits(UUID patientAppointmentId) throws BadRequestException, ResourceNotFoundException;

     PatientAppointmentImage addImage(UUID appointmentId, UUID imageId, String remarks) throws ResourceNotFoundException, BadRequestException;

     void removeImage(UUID appointmentImageId) throws ResourceNotFoundException, IOException;

     PatientAppointment checkInAppointment(UUID appointmentId) throws ResourceNotFoundException, BadRequestException;

     PatientAppointment markAppointmentAsAttended(UUID appointmentId, String remarks) throws ResourceNotFoundException, BadRequestException;

     PatientAppointment cancelAppointment(UUID appointmentId, String finalRemarks) throws ResourceNotFoundException, BadRequestException;
}
