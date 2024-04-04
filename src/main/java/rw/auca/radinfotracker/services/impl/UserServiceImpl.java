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
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.dtos.SetPasswordDTO;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.model.enums.ErrorCode;
import rw.auca.radinfotracker.repository.IUserAccountAuditRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.security.dtos.CustomUserDTO;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.IAuthenticationService;
import rw.auca.radinfotracker.services.IUserService;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final IJwtService jwtService;

    private final IAuthenticationService authenticationService;

    private final IUserAccountAuditRepository userAccountAuditRepository;

    public UserServiceImpl(IUserRepository userRepository, PasswordEncoder passwordEncoder, IJwtService jwtService, @Lazy IAuthenticationService authenticationService, IUserAccountAuditRepository userAccountAuditRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userAccountAuditRepository = userAccountAuditRepository;
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
        this.userRepository.save(user);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.CREATE, userDTO.getId(), userDTO.getFullNames(), "New user created", null);
        this.userAccountAuditRepository.save(audit);

        return user;
    }

    @Override
    public UserAccount activate(UUID userId) throws ResourceNotFoundException, BadRequestException {
        UserAccount user = getById(userId);

        if(!user.getStatus().equals(EUserStatus.INACTIVE))
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "exceptions.badRequest.userNot.Inactive");

        user.setStatus(EUserStatus.ACTIVE);
        authenticationService.invalidateUserLogin(user);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.APPROVE, userDTO.getId(), userDTO.getFullNames(), "User Activated", null);
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
        UserAccountAudit audit = new UserAccountAudit(user, EAuditType.DISABLE, userDTO.getId(), userDTO.getFullNames(), "User Deactivated", null);
        this.userAccountAuditRepository.save(audit);

        return this.userRepository.save(user);
    }

    @Override
    public Page<UserAccount> searchAll(String q, ERole role, EUserStatus status, Pageable pageable) {
        return this.userRepository.searchAll(q, status,role, pageable);
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
            UserAccountAudit audit = new UserAccountAudit(userAccount, EAuditType.UPDATE, userDTO.getId(), userDTO.getFullNames(), "Password reset", null);
            this.userAccountAuditRepository.save(audit);

            return userAccount;
        }else{
            throw new BadRequestException("exceptions.badRequest.userNot.pending.orActive");
        }
    }
}
