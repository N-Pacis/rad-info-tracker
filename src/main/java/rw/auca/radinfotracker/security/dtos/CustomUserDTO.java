package rw.auca.radinfotracker.security.dtos;

import lombok.*;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import java.util.UUID;


@Data
@Getter
@NoArgsConstructor
public class CustomUserDTO {
	private String fullNames;
	private UUID id;
	private String emailAddress;
	private EUserStatus status;

	public CustomUserDTO(UserAccount userAccount) {
		this.id = userAccount.getId();
		this.emailAddress = userAccount.getEmail();
		this.status = userAccount.getStatus();
		this.fullNames = userAccount.getFullName();
	}
}
