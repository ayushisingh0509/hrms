package hrms.hrms.security.auth.dto.response;

import hrms.hrms.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

	private String message;
	private boolean authenticated;
	private Integer userId;
	private String email;
	private Role role;
}