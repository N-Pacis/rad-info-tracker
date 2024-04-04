package rw.auca.radinfotracker.model.embeddables;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;


@Embeddable
@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public class UserEmbeddable {
    @Column(name = "_first_name")
    private String firstName;

    @Column(name = "_last_name")
    private String lastName;

    @Column(name="_email")
    private String email;

    @Column(name = "_phone_number")
    private String phoneNumber;

    @Column(name = "_role")
    private ERole role;

    @Column(name = "_status")
    private EUserStatus status;

    public UserEmbeddable(@NotNull UserAccount userAccount) {
        this.firstName = userAccount.getFirstName();
        this.lastName = userAccount.getLastName();
        this.email = userAccount.getEmail();
        this.phoneNumber = userAccount.getPhoneNumber();
        this.role = userAccount.getRole();
        this.status = userAccount.getStatus();
    }
}

