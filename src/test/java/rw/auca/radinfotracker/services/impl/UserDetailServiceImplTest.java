package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;
import rw.auca.radinfotracker.repository.IUserRepository;
import rw.auca.radinfotracker.utilities.Data;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest {

    @Mock private IUserRepository userRepository;

    private UserDetailServiceImpl userDetailService;

    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        userDetailService = new UserDetailServiceImpl(userRepository);
    }


    @Test
    void canLoadAUserByUsername() {
        UserAccount user = Data.createRadiologist();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailService.loadUserByUsername(user.getEmail());

        assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
        assertThat(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())).contains(user.getRole().toString());
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void shouldThrowUsernameNotFoundException() {
        String email = faker.internet().emailAddress();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class);
    }

}