package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Valid
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PatientAppointmentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File image;

    @Column
    private String remarks;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PatientAppointment appointment;

    public PatientAppointmentImage(File image, String remarks, PatientAppointment appointment){
        this.image = image;
        this.remarks = remarks;
        this.appointment = appointment;
    }

}
