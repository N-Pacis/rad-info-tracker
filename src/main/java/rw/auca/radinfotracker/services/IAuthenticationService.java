package rw.auca.radinfotracker.services;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.dtos.UpdatePasswordDTO;
import rw.auca.radinfotracker.security.dtos.LoginRequest;
import rw.auca.radinfotracker.security.dtos.LoginResponseDTO;

public interface IAuthenticationService {

    LoginResponseDTO signIn(LoginRequest request, HttpServletRequest httpRequest) throws ResourceNotFoundException;

    void signOut() throws ResourceNotFoundException;

    void invalidateUserLogin(UserAccount userAccount);


    @Transactional
    UserAccount updatePassword(UpdatePasswordDTO dto) throws ResourceNotFoundException, BadRequestException;
}
