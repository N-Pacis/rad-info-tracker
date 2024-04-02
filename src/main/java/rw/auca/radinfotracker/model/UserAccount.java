package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import rw.auca.radinfotracker.model.dtos.RegisterUserDTO;
import rw.auca.radinfotracker.model.enums.ELoginStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table
@Builder
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "last_name",nullable = false)
    private String lastName;

    @Transient
    private String fullName;

    @Column(name = "phone_number",nullable = false)
    private String phoneNumber;

    @Column(name = "email",nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private ERole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private EUserStatus status = EUserStatus.PENDING;

    @JsonIgnore
    @NotBlank
    @Column(name = "password",nullable = false)
    private String password;

    @JsonIgnore
    @Column(unique = true)
    private UUID sessionId;

    @Column
    @Enumerated(EnumType.STRING)
    private ELoginStatus loginStatus = ELoginStatus.INACTIVE;

    @JsonIgnore
    @Transient
    private Collection<GrantedAuthority> authorities;

    public UserAccount(String firstName, String lastName, String email, String phoneNumber, ERole role, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public UserAccount(RegisterUserDTO dto) {
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.phoneNumber = dto.getPhoneNumber();
        this.email = dto.getEmail();
        this.password = dto.getPassword();
    }

    public String getFullName() {
        return this.getFirstName()+" "+this.getLastName();
    }
}
