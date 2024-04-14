package rw.auca.radinfotracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.dtos.CancelOrFinalizeAppointmentDTO;
import rw.auca.radinfotracker.model.dtos.NewAppointmentImageDTO;
import rw.auca.radinfotracker.model.dtos.NewPatientAppointmentDTO;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.services.IPatientAppointmentService;
import rw.auca.radinfotracker.services.IPatientService;
import rw.auca.radinfotracker.utils.ApiResponse;
import rw.auca.radinfotracker.utils.Constants;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/patientAppointments")
public class PatientAppointmentController extends BaseController{
    private final IPatientAppointmentService patientAppointmentService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/byDate/{date}")
    public ResponseEntity<ApiResponse<Page<PatientAppointment>>> searchAll(
            @PathVariable(value = "date") LocalDate date,
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "status", required = false) EAppointmentStatus status,
            @RequestParam(value = "radiologist", required = false) UUID radiologist,
            @RequestParam(value = "technician", required = false) UUID technician,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) throws BadRequestException, ResourceNotFoundException {

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<PatientAppointment> appointments = this.patientAppointmentService.searchAllByDate(status, date, radiologist, technician, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(appointments, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('TECHNICIAN','RADIOLOGIST')")
    @GetMapping("/myAppointments/{date}")
    public ResponseEntity<ApiResponse<Page<PatientAppointment>>> getMyAppointments(
            @PathVariable(value = "date") LocalDate date,
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) throws ResourceNotFoundException {

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<PatientAppointment> appointments = this.patientAppointmentService.getAllMyAppointmentsByDate(date, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(appointments, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping(value = "/{id}/audits")
    public ResponseEntity<ApiResponse<List<PatientAppointmentAudit>>> getPatientAppointmentAudits(
            @PathVariable(value = "id") UUID id
    ) throws ResourceNotFoundException, BadRequestException {
        List<PatientAppointmentAudit> patientAudits = this.patientAppointmentService.getAppointmentAudits(id);
        return ResponseEntity.ok(
                new ApiResponse<>(patientAudits, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping(value = "/{id}/cancel")
    public ResponseEntity<ApiResponse<PatientAppointment>> cancelAppointment(
            @PathVariable(value = "id") UUID id,
            @Valid @RequestBody CancelOrFinalizeAppointmentDTO dto
            ) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = this.patientAppointmentService.cancelAppointment(id, dto.getFinalRemarks());
        return ResponseEntity.ok(
                new ApiResponse<>(appointment, localize("responses.updateEntitySuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('RADIOLOGIST')")
    @PutMapping(value = "/{id}/markAsAttended")
    public ResponseEntity<ApiResponse<PatientAppointment>> markAppointmentAsAttended(
            @PathVariable(value = "id") UUID id,
            @Valid @RequestBody CancelOrFinalizeAppointmentDTO dto
    ) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = this.patientAppointmentService.markAppointmentAsAttended(id, dto.getFinalRemarks());
        return ResponseEntity.ok(
                new ApiResponse<>(appointment, localize("responses.updateEntitySuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('TECHNICIAN')")
    @PutMapping(value = "/{id}/checkIn")
    public ResponseEntity<ApiResponse<PatientAppointment>> checkInPatientAppointment(
            @PathVariable(value = "id") UUID id
    ) throws ResourceNotFoundException, BadRequestException {
        PatientAppointment appointment = this.patientAppointmentService.checkInAppointment(id);
        return ResponseEntity.ok(
                new ApiResponse<>(appointment, localize("responses.updateEntitySuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('TECHNICIAN')")
    @PutMapping(value = "/{id}/addImage")
    public ResponseEntity<ApiResponse<PatientAppointmentImage>> addImageToAppointment(
            @PathVariable(value = "id") UUID id,
            @Valid @RequestBody NewAppointmentImageDTO dto
            ) throws ResourceNotFoundException, BadRequestException {
        PatientAppointmentImage image = this.patientAppointmentService.addImage(id, dto.getImageId(), dto.getRemarks());
        return ResponseEntity.ok(
                new ApiResponse<>(image, localize("responses.updateEntitySuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('TECHNICIAN')")
    @DeleteMapping(value = "/{imageId}/removeImage")
    public ResponseEntity<ApiResponse<?>> removeImageFromAppointment(
            @PathVariable(value = "imageId") UUID imageId
    ) throws IOException, ResourceNotFoundException {
        this.patientAppointmentService.removeImage(imageId);
        return ResponseEntity.ok(
                new ApiResponse<>(localize("responses.updateEntitySuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientAppointment>> create(@Valid @RequestBody NewPatientAppointmentDTO dto) throws BadRequestException, ResourceNotFoundException {
        PatientAppointment appointment = this.patientAppointmentService.create(dto);
        return ResponseEntity.ok(new ApiResponse<>(appointment,localize("responses.saveEntitySuccess"), HttpStatus.CREATED));
    }

    @Override
    protected String getEntityName() {
        return "Patient Appointment";
    }
}
