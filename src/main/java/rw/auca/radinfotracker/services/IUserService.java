package rw.auca.radinfotracker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.dtos.SetPasswordDTO;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserAccount getLoggedInUser() throws ResourceNotFoundException;

    UserAccount create(RegisterUserDTO dto) throws BadRequestException;

    UserAccount getById(UUID id) throws ResourceNotFoundException;

    UserAccount activate(UUID userId) throws ResourceNotFoundException, BadRequestException;

    UserAccount deactivate(UUID userId) throws ResourceNotFoundException, BadRequestException;

    Page<UserAccount> searchAll(String q, ERole role, EUserStatus status, Pageable pageable) throws ResourceNotFoundException;

    @Transactional
    UserAccount resetPassword(UUID id, SetPasswordDTO passwordDTO) throws ResourceNotFoundException, BadRequestException;

    Page<UserAccountAudit> getAuditByUser(UUID id, Pageable pageable) throws ResourceNotFoundException;

    Page<UserAccountLoginHistory> getUserLoginHistory(UUID userId, LocalDate date, Pageable pageable) throws ResourceNotFoundException;
}
