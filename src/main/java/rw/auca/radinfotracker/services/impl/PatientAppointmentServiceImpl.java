package rw.auca.radinfotracker.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.PatientAppointmentAudit;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.dtos.NewPatientAppointmentDTO;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.repository.IPatientAppointmentAuditRepository;
import rw.auca.radinfotracker.repository.IPatientAppointmentRepository;
import rw.auca.radinfotracker.services.IPatientAppointmentService;
import rw.auca.radinfotracker.services.IPatientService;
import rw.auca.radinfotracker.services.IUserService;
import rw.auca.radinfotracker.utils.RandomUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientAppointmentServiceImpl implements IPatientAppointmentService {

    private final IPatientAppointmentRepository patientAppointmentRepository;

    private final IPatientAppointmentAuditRepository patientAppointmentAuditRepository;

    private final IUserService userService;

    private final IPatientService patientService;

    public PatientAppointmentServiceImpl(IPatientAppointmentRepository patientAppointmentRepository, IPatientAppointmentAuditRepository patientAppointmentAuditRepository, IUserService userService, IPatientService patientService) {
        this.patientAppointmentRepository = patientAppointmentRepository;
        this.patientAppointmentAuditRepository = patientAppointmentAuditRepository;
        this.userService = userService;
        this.patientService = patientService;
    }

    @Override
    public PatientAppointment create(NewPatientAppointmentDTO dto) throws ResourceNotFoundException, BadRequestException {
        Patient patient = patientService.getById(dto.getPatientId());

        UserAccount radiologist = userService.getById(dto.getRadiologistId());
        if(!radiologist.getRole().equals(ERole.RADIOLOGIST)) throw new BadRequestException("exceptions.badRequest.appointment.invalidRadiologist");

        UserAccount technician = userService.getById(dto.getTechnicianId());
        if(technician.getRole().equals(ERole.TECHNICIAN)) throw new BadRequestException("exceptions.badRequest.appointment.invalidTechnician");

        if(LocalDate.now().isAfter(dto.getDate())) throw new BadRequestException("exceptions.badRequest.appointment.invalidDate");

        String refNumber = "APT-" + RandomUtil.randomNumber();

        while(patientAppointmentRepository.findByRefNumber(refNumber).isPresent()){
            refNumber = "APT-" + RandomUtil.randomNumber();
        }

        PatientAppointment patientAppointment = new PatientAppointment(refNumber, dto.getDate(), patient, radiologist, technician);

        return patientAppointmentRepository.save(patientAppointment);
    }

    @Override
    public Page<PatientAppointment> searchAllByDate(EAppointmentStatus status, LocalDate date, UUID radiologistId, UUID technicianId, Pageable pageable) throws ResourceNotFoundException, BadRequestException {
        UserAccount radiologist = userService.getById(radiologistId);
        if(!radiologist.getRole().equals(ERole.RADIOLOGIST)) throw new BadRequestException("exceptions.badRequest.appointment.invalidRadiologist");

        UserAccount technician = userService.getById(technicianId);
        if(technician.getRole().equals(ERole.TECHNICIAN)) throw new BadRequestException("exceptions.badRequest.appointment.invalidTechnician");

        return patientAppointmentRepository.searchAllByDate(status, date, radiologist, technician, pageable);
    }

    @Override
    public Page<PatientAppointment> getAllMyAppointmentsByDate(LocalDate date, Pageable pageable) throws ResourceNotFoundException {
        UserAccount user = userService.getLoggedInUser();

        if(user.getRole().equals(ERole.RADIOLOGIST)){
            return patientAppointmentRepository.findAllByDateAndRadiologist(date, user, pageable);
        }
        return patientAppointmentRepository.findAllByDateAndTechnician(date, user, pageable);
    }

    @Override
    public PatientAppointment getById(UUID id) throws ResourceNotFoundException {
        return patientAppointmentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("exceptions.notFound.appointment"));
    }

    @Override
    public List<PatientAppointmentAudit> getAppointmentAudits(UUID patientAppointmentId) throws ResourceNotFoundException {
        PatientAppointment appointment = getById(patientAppointmentId);
        return patientAppointmentAuditRepository.findAllByPatientAppointment(appointment);
    }
}
