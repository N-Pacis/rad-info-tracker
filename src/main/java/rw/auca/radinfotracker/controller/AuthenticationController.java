package rw.auca.radinfotracker.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.dtos.UpdatePasswordDTO;
import rw.auca.radinfotracker.security.dtos.LoginRequest;
import rw.auca.radinfotracker.security.dtos.LoginResponseDTO;
import rw.auca.radinfotracker.services.IAuthenticationService;
import rw.auca.radinfotracker.services.IUserService;
import rw.auca.radinfotracker.utils.ApiResponse;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController extends BaseController {
    private final IAuthenticationService authenticationService;
    private final IUserService userService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> signin(
            @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "Device-Type", required = false) String deviceType) throws ResourceNotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(authenticationService.signIn(request, userAgent, deviceType), localize("responses.getEntitySuccess"), HttpStatus.OK));
    }

    @PutMapping(path="/updatePassword")
    public ResponseEntity<ApiResponse<UserAccount>> changePassword(@Valid @RequestBody UpdatePasswordDTO dto) throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = this.authenticationService.updatePassword(dto);
        return ResponseEntity.ok(new ApiResponse<>(userAccount, localize("responses.updateEntitySuccess"), HttpStatus.OK));
    }

    @PostMapping("/signOut")
    public ResponseEntity<ApiResponse<Object>> signOut() throws ResourceNotFoundException {
        authenticationService.signOut();
        return ResponseEntity.ok(new ApiResponse<>(localize("responses.success"), HttpStatus.OK));
    }

    @GetMapping("/currentUser")
    public ResponseEntity<ApiResponse<UserAccount>> authUser() throws ResourceNotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(userService.getLoggedInUser(), localize("responses.getEntitySuccess"), HttpStatus.OK));
    }

    @Override
    protected String getEntityName() {
        return "Auth";
    }
}
