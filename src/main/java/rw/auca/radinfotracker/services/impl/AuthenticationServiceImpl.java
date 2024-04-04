package rw.auca.radinfotracker.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.InvalidCredentialsException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;
import rw.auca.radinfotracker.model.dtos.UpdatePasswordDTO;
import rw.auca.radinfotracker.model.enums.EAuditType;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.repository.IUserAccountAuditRepository;
import rw.auca.radinfotracker.repository.IUserAccountLoginHistoryRepository;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.security.dtos.*;
import rw.auca.radinfotracker.security.service.IJwtService;
import rw.auca.radinfotracker.services.IAuthenticationService;
import rw.auca.radinfotracker.services.IUserService;
import rw.auca.radinfotracker.utils.Constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final IUserRepository userRepository;
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final IUserService userService;

    private final PasswordEncoder passwordEncoder;

    private final IUserAccountLoginHistoryRepository userAccountLoginHistoryRepository;

    private final IUserAccountAuditRepository userAccountAuditRepository;

    @Override
    public LoginResponseDTO signIn(LoginRequest request, String userAgent, String deviceType) {

        UserAccount user = null;
        request.setLogin(request.getLogin().trim());
        request.setPassword(request.getPassword().trim());

        try{

            user = userRepository.findByEmail(request.getLogin()).orElseThrow(InvalidCredentialsException::new);

            if(user.getStatus().equals(EUserStatus.INACTIVE))
                throw new InvalidCredentialsException("exceptions.badRequest.accountLocked");

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));

            saveLoginHistory(user, userAgent, deviceType);

            user.setLastLogin(LocalDateTime.now(ZoneId.of("Africa/Kigali")));
            userRepository.save(user);

            var jwt = generateJWTToken(user);

            return LoginResponseDTO.builder().token(jwt).build();

        }catch(Exception e){
            log.info("Exception: " + e.getMessage());
            throw new InvalidCredentialsException("exceptions.invalidEmailPassword");
        }
    }

    @Override
    public void signOut() throws ResourceNotFoundException {
        UserAccount userAccount = userService.getLoggedInUser();

        invalidateUserLogin(userAccount);
    }

    @Override
    public void invalidateUserLogin(UserAccount userAccount){
        userAccount.setSessionId(null);
        userAccount.setLoginStatus(ELoginStatus.INACTIVE);

        userRepository.save(userAccount);
    }

    public void saveLoginHistory(UserAccount userAccount, String userAgent, String deviceType){
        UserAccountLoginHistory userAccountLoginHistory = new UserAccountLoginHistory(userAgent, deviceType, userAccount);;
        userAccountLoginHistoryRepository.save(userAccountLoginHistory);
    }

    @Transactional
    @Override
    public UserAccount updatePassword(UpdatePasswordDTO dto) throws ResourceNotFoundException, BadRequestException {
        UserAccount userAccount = userService.getLoggedInUser();

        if(!passwordEncoder.matches(dto.getOldPassword(), userAccount.getPassword())) {
            throw new BadRequestException("exceptions.badRequest.passwordMissMatch");
        }

        userAccount.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.userRepository.save(userAccount);

        CustomUserDTO userDTO = this.jwtService.extractLoggedInUser();
        UserAccountAudit audit = new UserAccountAudit(userAccount, EAuditType.UPDATE, userDTO.getId(), userDTO.getFullNames(), userDTO.getEmailAddress(), "Password updated", null);
        this.userAccountAuditRepository.save(audit);

        return userAccount;
    }


    private JwtAuthenticationResponse generateJWTToken(UserAccount user){
        List<GrantedAuthority> privileges = new ArrayList<>();
        privileges.add(new SimpleGrantedAuthority(user.getRole().toString()));

        user.setAuthorities(privileges);

        return JwtAuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(UserDetailsImpl.build(user))).tokenType(Constants.TOKEN_TYPE).build();
    }
}
