package rw.auca.radinfotracker.audits;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import rw.auca.radinfotracker.model.enums.EAuditType;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Setter
@Getter
@MappedSuperclass
public class AuditDetail  extends  UserDateAudit{

    private String observation;

    private EAuditType auditType;

    @JsonIgnore
    private Object supportingDocument;

}
