package rw.auca.radinfotracker.services.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.dtos.SetPasswordDTO;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.model.enums.ErrorCode;
import rw.auca.radinfotracker.repository.IUserAccountAuditRepository;
import rw.auca.radinfotracker.repository.IUserAccountLoginHistoryRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.IAuthenticationService;
import rw.auca.radinfotracker.services.IUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final IJwtService jwtService;

    private final IAuthenticationService authenticationService;

    private final IUserAccountAuditRepository userAccountAuditRepository;

    private final IUserAccountLoginHistoryRepository userAccountLoginHistoryRepository;

    public UserServiceImpl(IUserRepository userRepository, PasswordEncoder passwordEncoder, IJwtService jwtService, @Lazy IAuthenticationService authenticationService, IUserAccountAuditRepository userAccountAuditRepository, IUserAccountLoginHistoryRepository userAccountLoginHistoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userAccountAuditRepository = userAccountAuditRepository;
        this.userAccountLoginHistoryRepository = userAccountLoginHistoryRepository;
    }


    @Override
    public UserAccount getLoggedInUser() throws ResourceNotFoundException {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        Optional<UserAccount> findByEmail = userRepository.findByEmail(username);
        if (findByEmail.isPresent()) {
            return this.getById(findByEmail.get().getId());
        }
        else {
            return null;
        }
    }

    @Override
    public UserAccount getById(UUID id) throws ResourceNotFoundException {
        return this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("exceptions.notFound.user")
        );
    }

    @Override
    public UserAccount create(RegisterUserDTO dto) throws BadRequestException {
        dto.setEmail(dto.getEmail().trim());
        dto.setPassword(dto.getPassword().trim());

        Optional<UserAccount> duplicateEmailAddress = this.userRepository.findByEmail(dto.getEmail());
        if (duplicateEmailAddress.isPresent())
            throw new BadRequestException("exceptions.badRequest.emailExists");

        Optional<UserAccount> duplicatePhoneNumber = this.userRepository.findByPhoneNumber(dto.getPhoneNumber());
        if (duplicatePhoneNumber.isPresent())
            throw new BadRequestException("exceptions.badRequest.phoneExists");

        UserAccount user = new UserAccount(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(EUserStatus.ACTIVE);
        this.userRepository.save(user);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.CREATE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "New user created", null);
        this.userAccountAuditRepository.save(audit);

        return user;
    }

    @Override
    public UserAccount activate(UUID userId) throws ResourceNotFoundException, BadRequestException {
        UserAccount user = getById(userId);

        if(!user.getStatus().equals(EUserStatus.INACTIVE) && !user.getStatus().equals(EUserStatus.PENDING))
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "exceptions.badRequest.userNot.pending.orInactive");

        user.setStatus(EUserStatus.ACTIVE);
        authenticationService.invalidateUserLogin(user);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.APPROVE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "User Activated", null);
        this.userAccountAuditRepository.save(audit);

        return this.userRepository.save(user);
    }

    @Override
    public UserAccount deactivate(UUID userId) throws ResourceNotFoundException, BadRequestException {
        UserAccount user = getById(userId);

        if(!user.getStatus().equals(EUserStatus.ACTIVE) && !user.getStatus().equals(EUserStatus.PENDING))
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "exceptions.badRequest.userNot.pending.orActive");

        user.setStatus(EUserStatus.INACTIVE);
        authenticationService.invalidateUserLogin(user);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.DISABLE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "User Deactivated", null);
        this.userAccountAuditRepository.save(audit);

        return this.userRepository.save(user);
    }

    @Override
    public Page<UserAccount> searchAll(String q, ERole role, EUserStatus status, Pageable pageable) {
        return this.userRepository.searchAll(q, status,role, pageable);
    }

    @Override
    public List<UserAccount> getAllActiveUsersAsList(String q, ERole role) {
        return this.userRepository.searchAll(q, EUserStatus.ACTIVE,role);
    }

    @Override
    @Transactional
    public UserAccount resetPassword(UUID id, SetPasswordDTO passwordDTO) throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id.toString())
        );

        if (userAccount.getStatus().equals(EUserStatus.ACTIVE) || userAccount.getStatus().equals(EUserStatus.PENDING) ) {
            userAccount.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
            userAccount = this.userRepository.save(userAccount);

            authenticationService.invalidateUserLogin(userAccount);

            CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
            UserAccountAudit audit = new UserAccountAudit(userAccount, EAuditType.UPDATE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress() ,"Password reset", null);
            this.userAccountAuditRepository.save(audit);

            return userAccount;
        }else{
            throw new BadRequestException("exceptions.badRequest.userNot.pending.orActive");
        }
    }

    @Override
    public Page<UserAccountAudit> getAuditByUser(UUID id, Pageable pageable) throws ResourceNotFoundException {
        UserAccount user = getById(id);

        return userAccountAuditRepository.findAllByUserAccount(user, pageable);
    }

    @Override
    public Page<UserAccountLoginHistory> getUserLoginHistory(UUID userId, LocalDate date, Pageable pageable) throws ResourceNotFoundException {
        UserAccount userAccount = this.getById(userId);
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(23,59,59,9999999);
        return userAccountLoginHistoryRepository.findByUserAndCreatedAtBetween(userAccount, startTime, endTime, pageable);
    }

}
