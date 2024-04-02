package rw.auca.radinfotracker.audits;

import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@MappedSuperclass
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class UserDateAudit extends DateAudit {

	private UUID operatorId;

	private String operatorNames;

	private UUID userRoleId;

	private String userRoleName;

	private String operatorPrivilege;
}
