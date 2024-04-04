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
import rw.auca.radinfotracker.model.embeddables.UserEmbeddable;
import rw.auca.radinfotracker.model.enums.EAuditType;

import java.io.Serial;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class UserAccountAudit extends AuditDetail<UserEmbeddable> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    @GeneratedValue(generator = "UserAuditUUID")
    @GenericGenerator(name="UserAuditUUID", strategy="org.hibernate.id.UUIDGenerator")
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount userAccount;


    public UserAccountAudit(
            UserAccount userAccount, EAuditType auditType,
            UUID operatorId, String operatorNames,
            String observation, File supportingDocument
    ) {
        this.userAccount = userAccount;
        this.setSnapshot(new UserEmbeddable(userAccount));
        this.setAuditType(auditType);
        this.setOperatorId(operatorId);
        this.setOperationNames(operatorNames);
        this.setObservation(observation);
        this.setSupportingDocument(supportingDocument);
    }
}

