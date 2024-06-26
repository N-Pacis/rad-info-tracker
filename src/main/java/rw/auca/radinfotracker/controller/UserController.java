package rw.auca.radinfotracker.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import rw.auca.radinfotracker.model.PatientAudit;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.dtos.SetPasswordDTO;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.services.IUserService;
import rw.auca.radinfotracker.utils.ApiResponse;
import rw.auca.radinfotracker.utils.Constants;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController{
    private final IUserService userService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping(value = "/search")
    public ResponseEntity<ApiResponse<Page<UserAccount>>> searchAll(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "q",required = false,defaultValue = "") String query,
            @RequestParam(value = "role", required = false) ERole role,
            @RequestParam(value = "status", required = false) EUserStatus status,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) throws ResourceNotFoundException {

        Pageable pageable = PageRequest.of(page-1, limit);

        Page<UserAccount> users = this.userService.searchAll(query, role, status, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(users, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('FRONT_DESK','ADMIN')")
    @GetMapping(value = "/asList")
    public ResponseEntity<ApiResponse<List<UserAccount>>> searchAllAsList(
            @RequestParam(value = "q",required = false,defaultValue = "") String query,
            @RequestParam(value = "role", required = false) ERole role) {

        List<UserAccount> users = this.userService.getAllActiveUsersAsList(query, role);
        return ResponseEntity.ok(
                new ApiResponse<>(users, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @GetMapping(value = "/byId/{id}")
    public ResponseEntity<ApiResponse<UserAccount>> getUserById(
            @PathVariable() UUID id) throws ResourceNotFoundException {
        UserAccount user = this.userService.getById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(user, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterUserDTO dto) throws BadRequestException {
        this.userService.create(dto);
        return ResponseEntity.ok(new ApiResponse<>(localize("responses.saveEntitySuccess"), HttpStatus.CREATED));

    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping(path="/{id}/activate")
    public ResponseEntity<ApiResponse<UserAccount>> approveById(@PathVariable(value = "id") UUID id) throws ResourceNotFoundException, BadRequestException {
        UserAccount user = this.userService.activate(id);
        return ResponseEntity.ok(new ApiResponse<>(user, localize("responses.updateEntitySuccess"), HttpStatus.OK));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping(path="/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserAccount>> deactivateById(@PathVariable(value = "id") UUID id) throws ResourceNotFoundException, BadRequestException, BadRequestException {
        UserAccount user = this.userService.deactivate(id);
        return ResponseEntity.ok(new ApiResponse<>(user, localize("responses.updateEntitySuccess"), HttpStatus.OK));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping(path="/{id}/resetPassword")
    public ResponseEntity<ApiResponse<UserAccount>> resetPassword(@PathVariable(value = "id") UUID id, @Valid @RequestBody SetPasswordDTO dto) throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = this.userService.resetPassword(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(userAccount, localize("responses.updateEntitySuccess"), HttpStatus.OK));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping(value = "/{id}/audits")
    public ResponseEntity<ApiResponse<Page<UserAccountAudit>>> getUserAudit(
            @PathVariable(value = "id") UUID id,
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit
            ) throws ResourceNotFoundException {
        Sort sort = Sort.by(Sort.Direction.DESC, "doneAt");

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<UserAccountAudit> userAudits = this.userService.getAuditByUser(id, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(userAudits, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @GetMapping(path="/{id}/loginHistory")
    public ResponseEntity<ApiResponse<Page<UserAccountLoginHistory>>> getUserLoginHistory(
            @PathVariable(value = "id") UUID id,
            @RequestParam(value = "date") LocalDate date,
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit
    ) throws ResourceNotFoundException {
        Pageable pageable = (Pageable) PageRequest.of(page-1, limit, Sort.Direction.DESC,"createdAt");

        Page<UserAccountLoginHistory> loginHistory = this.userService.getUserLoginHistory(id, date, pageable);
        return ResponseEntity.ok(new ApiResponse<>(loginHistory, localize("responses.getListSuccess"), HttpStatus.OK));
    }

    @Override
    protected String getEntityName() {
        return "User";
    }
}
