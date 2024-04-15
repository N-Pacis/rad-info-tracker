package rw.auca.radinfotracker.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewPatientAppointmentDTO {

    @NotNull
    private UUID patientId;

    @NotNull
    private UUID radiologistId;

    @NotNull
    private UUID technicianId;

    @NotNull
    private UUID insuranceId;

    @NotNull
    private UUID imageTypeId;

    @NotNull
    private LocalDate date;
}
