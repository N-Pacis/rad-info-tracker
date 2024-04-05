package rw.auca.radinfotracker.model.embeddables;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.time.LocalDate;


@Embeddable
@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public class PatientAppointmentEmbeddable {
    @Column(name = "_ref_number")
    private String refNumber;

    @Column(name = "_date")
    private LocalDate date;

    @Column(name = "_status")
    private EAppointmentStatus status;

    public PatientAppointmentEmbeddable(@NotNull PatientAppointment patientAppointment) {
        this.refNumber = patientAppointment.getRefNumber();
        this.date = patientAppointment.getDate();
        this.status = patientAppointment.getStatus();
    }
}

