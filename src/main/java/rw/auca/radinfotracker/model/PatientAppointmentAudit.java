package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import rw.auca.radinfotracker.audits.AuditDetail;
import rw.auca.radinfotracker.model.embeddables.PatientAppointmentEmbeddable;
import rw.auca.radinfotracker.model.embeddables.PatientEmbeddable;
import rw.auca.radinfotracker.model.enums.EAuditType;

import java.io.Serial;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class PatientAppointmentAudit extends AuditDetail<PatientAppointmentEmbeddable> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    @GeneratedValue(generator = "PatientAppointmentAuditUUID")
    @GenericGenerator(name="PatientAppointmentAuditUUID", strategy="org.hibernate.id.UUIDGenerator")
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_appointment_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PatientAppointment patientAppointment;


    public PatientAppointmentAudit(
            PatientAppointment patientAppointment, EAuditType auditType,
            UUID operatorId, String operatorNames, String operatorEmail,
            String observation, File supportingDocument
    ) {
        this.patientAppointment = patientAppointment;
        this.setSnapshot(new PatientAppointmentEmbeddable(patientAppointment));
        this.setAuditType(auditType);
        this.setOperatorId(operatorId);
        this.setOperatorNames(operatorNames);
        this.setOperatorEmail(operatorEmail);
        this.setObservation(observation);
        this.setSupportingDocument(supportingDocument);
    }
}

