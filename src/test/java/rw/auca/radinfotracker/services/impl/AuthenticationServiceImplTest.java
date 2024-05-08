package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.InvalidCredentialsException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.dtos.UpdatePasswordDTO;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.repository.IUserAccountAuditRepository;
import rw.auca.radinfotracker.repository.IUserAccountLoginHistoryRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.dtos.LoginRequest;
import rw.auca.radinfotracker.security.dtos.LoginResponseDTO;
import rw.auca.radinfotracker.security.dtos.UserDetailsImpl;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.IUserService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock private IUserRepository userRepository;

    @Mock private IJwtService jwtService;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private IUserService userService;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private IUserAccountLoginHistoryRepository userAccountLoginHistoryRepository;

    @Mock private IUserAccountAuditRepository userAccountAuditRepository;

    private AuthenticationServiceImpl authenticationService;

    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(userRepository, jwtService, authenticationManager, userService, passwordEncoder, userAccountLoginHistoryRepository, userAccountAuditRepository);
    }

    @Test
    void signIn_WithValidCredentials_ReturnsJwtToken() {
        UserAccount userAccount = createUserAccount();
        LoginRequest loginRequest = new LoginRequest(userAccount.getEmail(), userAccount.getPassword());
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        String deviceType = "Desktop";

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(userAccount));
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn("generatedToken");

        LoginResponseDTO loginResponse = authenticationService.signIn(loginRequest, userAgent, deviceType);

        assertNotNull(loginResponse.getToken());
        assertEquals("generatedToken", loginResponse.getToken().getAccessToken());
        verify(userAccountLoginHistoryRepository, times(1)).save(any(UserAccountLoginHistory.class));
    }

    @Test
    void signIn_WithInvalidCredentials_ThrowsInvalidCredentialsException() {
        LoginRequest loginRequest = new LoginRequest("invalid@email.com", "wrongPassword");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.signIn(loginRequest, null, null));
    }

    @Test
    void signOut_WithLoggedInUser_InvalidatesUserLogin() throws ResourceNotFoundException {
        UserAccount userAccount = createUserAccount();
        userAccount.setLoginStatus(ELoginStatus.ACTIVE);

        when(userService.getLoggedInUser()).thenReturn(userAccount);

        authenticationService.signOut();

        verify(userRepository, times(1)).save(argThat(savedUser ->
                savedUser.getSessionId() == null && savedUser.getLoginStatus() == ELoginStatus.INACTIVE));
    }

    @Test
    void signOut_WithNoLoggedInUser_ThrowsResourceNotFoundException() throws ResourceNotFoundException {
        when(userService.getLoggedInUser()).thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> authenticationService.signOut());
    }

    @Test
    void updatePassword_WithValidOldPassword_PasswordUpdated() throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = createUserAccount();
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(userAccount.getPassword(), "newPassword");

        CustomUserDTO customUserDTO = new CustomUserDTO(userAccount);

        when(userService.getLoggedInUser()).thenReturn(userAccount);
        when(passwordEncoder.matches(updatePasswordDTO.getOldPassword(), userAccount.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(updatePasswordDTO.getNewPassword())).thenReturn("encodedNewPassword");
        when(jwtService.extractLoggedInUser()).thenReturn(customUserDTO);

        UserAccount updatedUserAccount = authenticationService.updatePassword(updatePasswordDTO);

        assertEquals("encodedNewPassword", updatedUserAccount.getPassword());
        verify(userAccountAuditRepository, times(1)).save(any(UserAccountAudit.class));
    }

    @Test
    void updatePassword_WithInvalidOldPassword_ThrowsBadRequestException() throws ResourceNotFoundException {
        UserAccount userAccount = createUserAccount();
        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO("wrongOldPassword", "newPassword");

        when(userService.getLoggedInUser()).thenReturn(userAccount);
        when(passwordEncoder.matches(updatePasswordDTO.getOldPassword(), userAccount.getPassword())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authenticationService.updatePassword(updatePasswordDTO));
        verify(userAccountAuditRepository, never()).save(any(UserAccountAudit.class));
    }

    private UserAccount createUserAccount() {
        return new UserAccount(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), faker.phoneNumber().phoneNumber(), ERole.RADIOLOGIST, EUserStatus.ACTIVE, ELoginStatus.INACTIVE, faker.internet().password());
    }
}