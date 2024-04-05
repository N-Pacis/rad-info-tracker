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
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;
import rw.auca.radinfotracker.services.IInsuranceService;
import rw.auca.radinfotracker.utils.ApiResponse;
import rw.auca.radinfotracker.utils.Constants;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/insurances")
public class InsuranceController extends BaseController{
    private final IInsuranceService insuranceService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Insurance>>> searchAll(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "status", required = false) EInsuranceStatus status,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) throws BadRequestException, ResourceNotFoundException {

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<Insurance> insurances = this.insuranceService.getAll(query, status, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(insurances, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Insurance>>> getAllActiveInsurances(
            @RequestParam(value = "query", required = false, defaultValue = "") String query) {

        List<Insurance> insurances = this.insuranceService.getAll(query);
        return ResponseEntity.ok(
                new ApiResponse<>(insurances, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Insurance>> register(@Valid @RequestBody NewInsuranceDTO dto) throws BadRequestException {
        Insurance insurance = this.insuranceService.register(dto);
        return ResponseEntity.ok(new ApiResponse<>(insurance,localize("responses.saveEntitySuccess"), HttpStatus.CREATED));
    }

    @Override
    protected String getEntityName() {
        return "Insurance";
    }
}
