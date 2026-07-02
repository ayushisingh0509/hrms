package hrms.hrms.security.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import hrms.hrms.entity.User;
import hrms.hrms.security.auth.dto.request.LoginRequest;
import hrms.hrms.security.auth.dto.response.AuthenticationResponse;
import hrms.hrms.security.auth.exception.AuthenticationFailedException;
import hrms.hrms.security.model.CustomUserDetails;

@Service
public class AuthenticationService {

	private final AuthenticationManager authenticationManager;

	public AuthenticationService(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public AuthenticationResponse login(LoginRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
			CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
			User user = principal.getUser();
			return new AuthenticationResponse("Login successful.", true, user.getId(), user.getEmail(), user.getRole());
		} catch (AuthenticationException ex) {
			throw new AuthenticationFailedException("Invalid email or password.");
		}
	}
}