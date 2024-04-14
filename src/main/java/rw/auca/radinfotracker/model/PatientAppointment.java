package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import rw.auca.radinfotracker.audits.TimestampAudit;
import rw.auca.radinfotracker.model.dtos.NewPatientDTO;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table
@Builder
public class PatientAppointment extends TimestampAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "ref_number", nullable = false, unique = true)
    private String refNumber;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private EAppointmentStatus status = EAppointmentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Insurance insurance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radiologist_user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount radiologist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount technician;

    @Column
    private String finalRemarks;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PatientAppointmentImage> images;

    public PatientAppointment(String refNumber, LocalDate date, Patient patient, Insurance insurance, UserAccount radiologist, UserAccount technician){
        this.refNumber = refNumber;
        this.date = date;
        this.patient = patient;
        this.insurance = insurance;
        this.radiologist = radiologist;
        this.technician = technician;
    }
}
