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
import rw.auca.radinfotracker.model.enums.EPaymentStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IPatientAppointmentService {
     PatientAppointment create(NewPatientAppointmentDTO dto) throws ResourceNotFoundException, BadRequestException;

     Page<PatientAppointment> getAllMyAppointmentsByDate(LocalDate date, Pageable pageable) throws ResourceNotFoundException;

     PatientAppointment getById(UUID id) throws ResourceNotFoundException;

     Page<PatientAppointmentAudit> getAppointmentAudits(UUID patientAppointmentId, Pageable pageable) throws BadRequestException, ResourceNotFoundException;

     PatientAppointmentImage addImage(UUID appointmentId, UUID imageId, String remarks) throws ResourceNotFoundException, BadRequestException;

     void removeImage(UUID appointmentImageId) throws ResourceNotFoundException, IOException;

     PatientAppointment checkInAppointment(UUID appointmentId) throws ResourceNotFoundException, BadRequestException;

     PatientAppointment markAppointmentAsConsulted(UUID appointmentId, String remarks) throws ResourceNotFoundException, BadRequestException;

    PatientAppointment markAppointmentAsQualityChecked(UUID appointmentId) throws ResourceNotFoundException, BadRequestException;

    PatientAppointment markAppointmentAsPaid(UUID appointmentId) throws ResourceNotFoundException, BadRequestException;

    PatientAppointment cancelAppointment(UUID appointmentId, String finalRemarks) throws ResourceNotFoundException, BadRequestException;
}
