package rw.auca.radinfotracker.audits;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import rw.auca.radinfotracker.model.File;
import rw.auca.radinfotracker.model.enums.EAuditType;

import java.time.LocalDateTime;
import java.util.UUID;


@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
public abstract class AuditDetail<T> {

    private String observation;

    @Embedded
    private T snapshot;

    @Enumerated(EnumType.STRING)
    private EAuditType auditType;

    @ManyToOne
    private File supportingDocument;

    private UUID operatorId;

    private String operationNames;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime doneAt;
}
