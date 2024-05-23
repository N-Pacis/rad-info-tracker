package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import rw.auca.radinfotracker.audits.TimestampAudit;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Patient extends TimestampAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "ref_number", nullable = false, unique = true)
    private String refNumber;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "last_name",nullable = false)
    private String lastName;

    @Column(name = "phone_number",nullable = false)
    private String phoneNumber;

    @JsonFormat(pattern="yyyy-MM-dd")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private EPatientStatus status = EPatientStatus.ACTIVE;

    @Column(name = "address",nullable = false)
    private String address;

    public Patient(String refNumber, String firstName, String lastName,String phoneNumber, LocalDate dateOfBirth, String address) {
        this.refNumber = refNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public Patient(NewPatientDTO dto, String refNumber){
        this.refNumber = refNumber;
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.phoneNumber = dto.getPhoneNumber();
        this.dateOfBirth = dto.getDateOfBirth();
        this.address = dto.getAddress();
    }
}
