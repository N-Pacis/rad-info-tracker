package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.dtos.SetPasswordDTO;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.repository.IUserAccountAuditRepository;
import rw.auca.radinfotracker.repository.IUserAccountLoginHistoryRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.IAuthenticationService;
import rw.auca.radinfotracker.utilities.Data;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private IUserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private IJwtService jwtService;

    @Mock private IAuthenticationService authenticationService;

    @Mock private IUserAccountAuditRepository userAccountAuditRepository;

    @Mock private IUserAccountLoginHistoryRepository userAccountLoginHistoryRepository;

    private UserServiceImpl userService;

    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtService, authenticationService, userAccountAuditRepository, userAccountLoginHistoryRepository);
    }

    @Test
    void getLoggedInUser_WithValidPrincipal_ReturnsUserAccount() throws ResourceNotFoundException {
        UserAccount userAccount = Data.createRadiologist();
        UserDetails userDetails = new User(userAccount.getEmail(), userAccount.getPassword(), new ArrayList<>());

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(userAccount.getEmail())).thenReturn(Optional.of(userAccount));
        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));

        UserAccount loggedInUser = userService.getLoggedInUser();

        assertEquals(userAccount, loggedInUser);
    }

    @Test
    void getLoggedInUser_WithInvalidPrincipal_ReturnsNull() throws ResourceNotFoundException {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("invalidPrincipal");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserAccount loggedInUser = userService.getLoggedInUser();

        assertNull(loggedInUser);
    }

    @Test
    void getById_WithValidId_ReturnsUserAccount() throws ResourceNotFoundException {
        UserAccount userAccount = Data.createRadiologist();
        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));

        UserAccount foundUserAccount = userService.getById(userAccount.getId());

        assertEquals(userAccount, foundUserAccount);
    }

    @Test
    void getById_WithInvalidId_ThrowsResourceNotFoundException() {
        UUID invalidId = UUID.randomUUID();
        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(invalidId));
    }

    @Test
    void create_WithUniqueEmailAndPhoneNumber_CreatesUserAccount() throws BadRequestException {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.phoneNumber().phoneNumber(),
                "password",
                ERole.TECHNICIAN
        );

        UserAccount userAccount = Data.createRadiologist();
        CustomUserDTO customUserDTO = new CustomUserDTO(userAccount);

        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(registerUserDTO.getPhoneNumber())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(jwtService.extractLoggedInUser()).thenReturn(customUserDTO);

        UserAccount createdUserAccount = userService.create(registerUserDTO);

        assertNotNull(createdUserAccount);
        assertEquals(registerUserDTO.getFirstName(), createdUserAccount.getFirstName());
        assertEquals(registerUserDTO.getLastName(), createdUserAccount.getLastName());
        assertEquals(registerUserDTO.getEmail(), createdUserAccount.getEmail());
        assertEquals(registerUserDTO.getPhoneNumber(), createdUserAccount.getPhoneNumber());
        assertEquals("encodedPassword", createdUserAccount.getPassword());
        verify(userAccountAuditRepository, times(1)).save(any(UserAccountAudit.class));
    }

    @Test
    void create_WithDuplicateEmail_ThrowsBadRequestException() {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.phoneNumber().phoneNumber(),
                "password",
                ERole.RADIOLOGIST
        );

        UserAccount existingUserAccount = Data.createRadiologist();
        existingUserAccount.setEmail(registerUserDTO.getEmail());
        existingUserAccount.setPhoneNumber(registerUserDTO.getPhoneNumber());

        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.of(existingUserAccount));

        assertThrows(BadRequestException.class, () -> userService.create(registerUserDTO));
        verify(userAccountAuditRepository, never()).save(any(UserAccountAudit.class));
    }

    @Test
    void create_WithDuplicatePhoneNumber_ThrowsBadRequestException() {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                faker.phoneNumber().phoneNumber(),
                "password",
                ERole.RADIOLOGIST
        );

        UserAccount existingUserAccount = Data.createRadiologist();
        existingUserAccount.setEmail(registerUserDTO.getEmail());
        existingUserAccount.setPhoneNumber(registerUserDTO.getPhoneNumber());

        when(userRepository.findByPhoneNumber(registerUserDTO.getPhoneNumber())).thenReturn(Optional.of(existingUserAccount));

        assertThrows(BadRequestException.class, () -> userService.create(registerUserDTO));
        verify(userAccountAuditRepository, never()).save(any(UserAccountAudit.class));
    }

    @Test
    void activate_WithInactiveUser_ActivatesUser() throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = Data.createRadiologist();
        userAccount.setStatus(EUserStatus.INACTIVE);

        CustomUserDTO customUserDTO = new CustomUserDTO(userAccount);

        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));
        when(jwtService.extractLoggedInUser()).thenReturn(customUserDTO);

        userService.activate(userAccount.getId());

        verify(authenticationService, times(1)).invalidateUserLogin(userAccount);
        verify(userAccountAuditRepository, times(1)).save(any(UserAccountAudit.class));
    }

    @Test
    void activate_WithNonInactiveUser_ThrowsBadRequestException(){
        UserAccount userAccount = Data.createRadiologist();
        userAccount.setStatus(EUserStatus.ACTIVE);

        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));

        assertThrows(BadRequestException.class, () -> userService.activate(userAccount.getId()));
        verify(authenticationService, never()).invalidateUserLogin(any(UserAccount.class));
        verify(userAccountAuditRepository, never()).save(any(UserAccountAudit.class));
    }

    @Test
    void deactivate_WithActiveOrPendingUser_DeactivatesUser() throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = Data.createRadiologist();
        userAccount.setStatus(EUserStatus.ACTIVE);

        CustomUserDTO customUserDTO = new CustomUserDTO(userAccount);

        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));
        when(jwtService.extractLoggedInUser()).thenReturn(customUserDTO);

        userService.deactivate(userAccount.getId());

        verify(authenticationService, times(1)).invalidateUserLogin(userAccount);
        verify(userAccountAuditRepository, times(1)).save(any(UserAccountAudit.class));
    }

    @Test
    void deactivate_WithInactiveUser_ThrowsBadRequestException() {
        UserAccount userAccount = Data.createRadiologist();
        userAccount.setStatus(EUserStatus.INACTIVE);

        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));

        assertThrows(BadRequestException.class, () -> userService.deactivate(userAccount.getId()));
        verify(authenticationService, never()).invalidateUserLogin(any(UserAccount.class));
        verify(userAccountAuditRepository, never()).save(any(UserAccountAudit.class));
    }

    @Test
    void resetPassword_WithActiveOrPendingUser_ResetsPassword() throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = Data.createRadiologist();
        userAccount.setStatus(EUserStatus.ACTIVE);
        SetPasswordDTO setPasswordDTO = new SetPasswordDTO("newPassword");

        CustomUserDTO customUserDTO = new CustomUserDTO(userAccount);

        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));
        when(userRepository.save(any(UserAccount.class))).thenReturn((userAccount));
        when(passwordEncoder.encode(setPasswordDTO.getNewPassword())).thenReturn("encodedPassword");
        when(jwtService.extractLoggedInUser()).thenReturn(customUserDTO);

        userService.resetPassword(userAccount.getId(), setPasswordDTO);

        verify(authenticationService, times(1)).invalidateUserLogin(userAccount);
        verify(userAccountAuditRepository, times(1)).save(any(UserAccountAudit.class));
    }

    @Test
    void resetPassword_WithInactiveUser_ThrowsBadRequestException() {
        UserAccount userAccount = Data.createRadiologist();
        userAccount.setStatus(EUserStatus.INACTIVE);
        SetPasswordDTO setPasswordDTO = new SetPasswordDTO("newPassword");

        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));

        assertThrows(BadRequestException.class, () -> userService.resetPassword(userAccount.getId(), setPasswordDTO));
        verify(authenticationService, never()).invalidateUserLogin(any(UserAccount.class));
        verify(userAccountAuditRepository, never()).save(any(UserAccountAudit.class));
    }

    @Test
    void getAuditByUser_WithValidUserId_ReturnsAuditRecords() throws ResourceNotFoundException {
        UserAccount userAccount = Data.createTechnician();
        Pageable pageable = PageRequest.of(0, 10);

        List<UserAccountAudit> auditRecords = Arrays.asList(
                new UserAccountAudit(userAccount, EAuditType.CREATE, UUID.randomUUID(), "John Doe", "john.doe@example.com", "User created", null),
                new UserAccountAudit(userAccount, EAuditType.UPDATE, UUID.randomUUID(), "Jane Smith", "jane.smith@example.com", "Password updated", null)
        );

        Page<UserAccountAudit> expectedPage = new PageImpl<>(auditRecords, pageable, auditRecords.size());
        when(userRepository.findById(userAccount.getId())).thenReturn(Optional.of(userAccount));
        when(userAccountAuditRepository.findAllByUserAccount(userAccount, pageable)).thenReturn(expectedPage);

        Page<UserAccountAudit> result = userService.getAuditByUser(userAccount.getId(), pageable);

        assertEquals(expectedPage, result);
    }

    @Test
    void getAuditByUser_WithInvalidUserId_ThrowsResourceNotFoundException() {
        UUID invalidUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getAuditByUser(invalidUserId, pageable));
    }

    @Test
    void getUserLoginHistory_WithValidUserIdAndDate_ReturnsLoginHistory() throws ResourceNotFoundException {
        UUID userId = UUID.randomUUID();
        UserAccount userAccount = Data.createRadiologist();
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        List<UserAccountLoginHistory> loginHistories = Arrays.asList(
                new UserAccountLoginHistory("userAgent1", "deviceType1", userAccount),
                new UserAccountLoginHistory("userAgent2", "deviceType2", userAccount)
        );

        Page<UserAccountLoginHistory> expectedPage = new PageImpl<>(loginHistories, pageable, loginHistories.size());
        when(userRepository.findById(userId)).thenReturn(Optional.of(userAccount));
        when(userAccountLoginHistoryRepository.findByUserAndCreatedAtBetween(
                userAccount, date.atStartOfDay(), date.atTime(23, 59, 59, 9999999), pageable)).thenReturn(expectedPage);

        Page<UserAccountLoginHistory> result = userService.getUserLoginHistory(userId, date, pageable);

        assertEquals(expectedPage, result);
    }

    @Test
    void getUserLoginHistory_WithInvalidUserId_ThrowsResourceNotFoundException() {
        UUID invalidUserId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserLoginHistory(invalidUserId, date, pageable));
    }
}