package rw.auca.radinfotracker.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.dtos.NewPatientAppointmentDTO;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.EPaymentStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.repository.IPatientAppointmentAuditRepository;
import rw.auca.radinfotracker.repository.IPatientAppointmentImageRepository;
import rw.auca.radinfotracker.repository.IPatientAppointmentRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.*;
import rw.auca.radinfotracker.utils.RandomUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientAppointmentServiceImpl implements IPatientAppointmentService {

    private final IPatientAppointmentRepository patientAppointmentRepository;

    private final IPatientAppointmentAuditRepository patientAppointmentAuditRepository;

    private final IUserService userService;

    private final IPatientService patientService;

    private final IJwtService jwtService;

    private final IInsuranceService insuranceService;

    private final FileService fileService;

    private final IPatientAppointmentImageRepository patientAppointmentImageRepository;

    private final IImageTypeService imageTypeService;

    public PatientAppointmentServiceImpl(IPatientAppointmentRepository patientAppointmentRepository, IPatientAppointmentAuditRepository patientAppointmentAuditRepository, IUserService userService, IPatientService patientService, IJwtService jwtService, IInsuranceService insuranceService, FileService fileService, IPatientAppointmentImageRepository patientAppointmentImageRepository, IImageTypeService imageTypeService) {
        this.patientAppointmentRepository = patientAppointmentRepository;
        this.patientAppointmentAuditRepository = patientAppointmentAuditRepository;
        this.userService = userService;
        this.patientService = patientService;
        this.jwtService = jwtService;
        this.insuranceService = insuranceService;
        this.fileService = fileService;
        this.patientAppointmentImageRepository = patientAppointmentImageRepository;
        this.imageTypeService = imageTypeService;
    }

    @Override
    public PatientAppointment create(NewPatientAppointmentDTO dto) throws ResourceNotFoundException, BadRequestException {
        Patient patient = patientService.getById(dto.getPatientId());

        UserAccount radiologist = userService.getById(dto.getRadiologistId());
        if(!radiologist.getRole().equals(ERole.RADIOLOGIST)) throw new BadRequestException("exceptions.badRequest.appointment.invalidRadiologist");

        UserAccount technician = userService.getById(dto.getTechnicianId());
        if(!technician.getRole().equals(ERole.TECHNICIAN)) throw new BadRequestException("exceptions.badRequest.appointment.invalidTechnician");

        Insurance insurance = insuranceService.getById(dto.getInsuranceId());

        ImageType imageType = imageTypeService.getById(dto.getImageTypeId());

        if(LocalDate.now().isAfter(dto.getDate())) throw new BadRequestException("exceptions.badRequest.appointment.invalidDate");

        String refNumber = "APT-" + RandomUtil.randomNumber();

        while(patientAppointmentRepository.findByRefNumber(refNumber).isPresent()){
            refNumber = "APT-" + RandomUtil.randomNumber();
        }

        PatientAppointment patientAppointment = new PatientAppointment(refNumber, dto.getDate(), patient, insurance, imageType,radiologist, technician);
        patientAppointment.setAmountToPay(imageType.getTotalCost() * (1.00 - insurance.getRate()));
        patientAppointment = patientAppointmentRepository.save(patientAppointment);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        PatientAppointmentAudit audit = new PatientAppointmentAudit(patientAppointment, EAuditType.CREATE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "Patient Appointment created", null);
        this.patientAppointmentAuditRepository.save(audit);

        return patientAppointment;
    }

    @Override
    public Page<PatientAppointment> searchAllByDate(EAppointmentStatus status, EPaymentStatus paymentStatus, LocalDate date, UUID radiologistId, UUID technicianId, Pageable pageable) throws ResourceNotFoundException, BadRequestException {
        UserAccount radiologist = userService.getById(radiologistId);
        if(!radiologist.getRole().equals(ERole.RADIOLOGIST)) throw new BadRequestException("exceptions.badRequest.appointment.invalidRadiologist");

        UserAccount technician = userService.getById(technicianId);
        if(!technician.getRole().equals(ERole.TECHNICIAN)) throw new BadRequestException("exceptions.badRequest.appointment.invalidTechnician");

        return patientAppointmentRepository.searchAllByDate(status, paymentStatus, date, radiologist, technician, pageable);
    }

    @Override
    public Page<PatientAppointment> getAllMyAppointmentsByDate(LocalDate date, Pageable pageable) throws ResourceNotFoundException {
        UserAccount user = userService.getLoggedInUser();

        if(user.getRole().equals(ERole.RADIOLOGIST)){
            return patientAppointmentRepository.findAllByDateAndRadiologistAndStatus(date, user, EAppointmentStatus.QUALITY_CHECKED, pageable);
        }
        else if(user.getRole().equals(ERole.TECHNICIAN)){
            return patientAppointmentRepository.findAllByDateAndTechnicianAndStatus(date, user, EAppointmentStatus.PENDING, pageable);
        }
        else if(user.getRole().equals(ERole.QUALITY_ASSURANCE)){
            return patientAppointmentRepository.findAllByDateAndStatus(date, EAppointmentStatus.ATTENDED, pageable);
        }
        else if(user.getRole().equals(ERole.FINANCE)){
            return patientAppointmentRepository.findAllByDateAndStatus(date, EAppointmentStatus.CONSULTED, pageable);
        }
        return null;
    }

    @Override
    public PatientAppointment getById(UUID id) throws ResourceNotFoundException {
        return patientAppointmentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("exceptions.notFound.appointment"));
    }

    @Override
    public Page<PatientAppointmentAudit> getAppointmentAudits(UUID patientAppointmentId, Pageable pageable) throws ResourceNotFoundException {
        PatientAppointment appointment = getById(patientAppointmentId);
        return patientAppointmentAuditRepository.findAllByPatientAppointment(appointment, pageable);
    }

    @Override
    public PatientAppointmentImage addImage(UUID appointmentId, UUID imageId, String remarks) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = getById(appointmentId);
        if(!appointment.getStatus().equals(EAppointmentStatus.PENDING))
            throw new BadRequestException("exceptions.badRequest.appointment.notPending");

        File image = fileService.findById(imageId);

        PatientAppointmentImage appointmentImage = new PatientAppointmentImage(image, remarks, appointment);
        return patientAppointmentImageRepository.save(appointmentImage);
    }

    private PatientAppointmentImage getPatientAppointmentImageById(UUID appointmentImageId) throws ResourceNotFoundException {
        return patientAppointmentImageRepository.findById(appointmentImageId).orElseThrow(()->new ResourceNotFoundException("exceptions.notFound.appointment.image"));
    }

     @Override
    public void removeImage(UUID appointmentImageId) throws ResourceNotFoundException, IOException {
        PatientAppointmentImage appointmentImage = getPatientAppointmentImageById(appointmentImageId);

        patientAppointmentImageRepository.delete(appointmentImage);
        fileService.deleteById(appointmentImage.getImage().getId());
    }

    @Override
    public PatientAppointment checkInAppointment(UUID appointmentId) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = getById(appointmentId);
        if(patientAppointmentImageRepository.countAllByAppointment(appointment) < 1)
            throw new BadRequestException("exceptions.badRequest.appointment.noImage");

        appointment.setStatus(EAppointmentStatus.ATTENDED);

        return patientAppointmentRepository.save(appointment);
    }

    @Override
    public PatientAppointment markAppointmentAsConsulted(UUID appointmentId, String remarks) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = getById(appointmentId);
        if(!appointment.getStatus().equals(EAppointmentStatus.QUALITY_CHECKED))
            throw new BadRequestException("exceptions.badRequest.appointment.noQualityChecked");

        appointment.setFinalRemarks(remarks);
        appointment.setStatus(EAppointmentStatus.CONSULTED);

        return patientAppointmentRepository.save(appointment);
    }

    @Override
    public PatientAppointment markAppointmentAsQualityChecked(UUID appointmentId) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = getById(appointmentId);
        if(!appointment.getStatus().equals(EAppointmentStatus.ATTENDED))
            throw new BadRequestException("exceptions.badRequest.appointment.notAttended");

        appointment.setStatus(EAppointmentStatus.QUALITY_CHECKED);

        return patientAppointmentRepository.save(appointment);
    }

    @Override
    public PatientAppointment markAppointmentAsPaid(UUID appointmentId) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = getById(appointmentId);
        if(!appointment.getStatus().equals(EAppointmentStatus.CONSULTED))
            throw new BadRequestException("exceptions.badRequest.appointment.notConsulted");

        if(appointment.getPaymentStatus().equals(EPaymentStatus.PAID))
            throw new BadRequestException("exceptions.badRequest.appointment.paid");

        appointment.setPaymentStatus(EPaymentStatus.PAID);

        return patientAppointmentRepository.save(appointment);
    }

    @Override
    public PatientAppointment cancelAppointment(UUID appointmentId, String finalRemarks) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = getById(appointmentId);
        if(!appointment.getStatus().equals(EAppointmentStatus.PENDING))
            throw new BadRequestException("exceptions.badRequest.appointment.notPending");

        appointment.setFinalRemarks(finalRemarks);
        appointment.setStatus(EAppointmentStatus.CANCELLED);

        return patientAppointmentRepository.save(appointment);
    }
}
