package rw.auca.radinfotracker.controller;

import lombok.RequiredArgsConstructor;
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
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.services.IPatientService;
import rw.auca.radinfotracker.utils.ApiResponse;
import rw.auca.radinfotracker.utils.Constants;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController extends BaseController{
    private final IPatientService patientService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Patient>>> searchAll(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "q",required = false,defaultValue = "") String query,
            @RequestParam(value = "status", required = false) EPatientStatus status,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<Patient> patients = this.patientService.getAll(query, status, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(patients, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping(value = "/{id}/audits")
    public ResponseEntity<ApiResponse<List<PatientAudit>>> getPatientAudit(
            @PathVariable(value = "id") UUID id
    ) throws ResourceNotFoundException {
        List<PatientAudit> patientAudits = this.patientService.getAuditByPatient(id);
        return ResponseEntity.ok(
                new ApiResponse<>(patientAudits, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Patient>> register(@Valid @RequestBody NewPatientDTO dto) throws BadRequestException {
        Patient patient = this.patientService.register(dto);
        return ResponseEntity.ok(new ApiResponse<>(patient,localize("responses.saveEntitySuccess"), HttpStatus.CREATED));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping(path="/{id}/activate")
    public ResponseEntity<ApiResponse<Patient>> approveById(@PathVariable(value = "id") UUID id) throws ResourceNotFoundException, BadRequestException {
        Patient patient = this.patientService.activate(id);
        return ResponseEntity.ok(new ApiResponse<>(patient, localize("responses.updateEntitySuccess"), HttpStatus.OK));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping(path="/{id}/deactivate")
    public ResponseEntity<ApiResponse<Patient>> deactivateById(@PathVariable(value = "id") UUID id) throws ResourceNotFoundException, BadRequestException, BadRequestException {
        Patient patient = this.patientService.deactivate(id);
        return ResponseEntity.ok(new ApiResponse<>(patient, localize("responses.updateEntitySuccess"), HttpStatus.OK));
    }

    @Override
    protected String getEntityName() {
        return "Patient";
    }
}
