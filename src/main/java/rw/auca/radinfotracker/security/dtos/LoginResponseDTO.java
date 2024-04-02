package rw.auca.radinfotracker.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.auca.radinfotracker.security.dtos.JwtAuthenticationResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
	
	private JwtAuthenticationResponse token;

}
