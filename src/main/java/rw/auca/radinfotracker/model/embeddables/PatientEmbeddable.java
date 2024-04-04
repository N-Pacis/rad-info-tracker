package rw.auca.radinfotracker.model.embeddables;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.EPatientStatus;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import java.time.LocalDate;


@Embeddable
@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public class PatientEmbeddable {
    @Column(name = "_ref_number")
    private String refNumber;

    @Column(name = "_first_name")
    private String firstName;

    @Column(name = "_last_name")
    private String lastName;

    @Column(name = "_phone_number")
    private String phoneNumber;

    @Column(name = "_address")
    private String address;

    @Column(name = "_date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "_status")
    private EPatientStatus status;

    public PatientEmbeddable(@NotNull Patient patient) {
        this.firstName = patient.getFirstName();
        this.lastName = patient.getLastName();
        this.phoneNumber = patient.getPhoneNumber();
        this.address = patient.getAddress();
        this.dateOfBirth = patient.getDateOfBirth();
        this.status = patient.getStatus();
    }
}

