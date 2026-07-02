package hrms.hrms.security.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.UserDao;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User createUser(String email, String rawPassword, Role role) {
		User user = new User();
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(role);
		user.setEnabled(true);
		user.setAccountNonLocked(true);
		user.setAccountNonExpired(true);
		user.setCredentialsNonExpired(true);
		return userDao.save(user);
	}

	@Test
	void should_login_successfully() throws Exception {
		String email = "login-success@test.com";
		User user = createUser(email, "Secret123", Role.EMPLOYER);

		String json = """
		{
		    "email": "%s",
		    "password": "Secret123"
		}
		""".formatted(email);

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Login successful."))
				.andExpect(jsonPath("$.authenticated").value(true))
				.andExpect(jsonPath("$.userId").value(user.getId()))
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.role").value("EMPLOYER"));
	}

	@Test
	void should_return_unauthorized_for_wrong_password() throws Exception {
		String email = "wrong-password@test.com";
		createUser(email, "Secret123", Role.CANDIDATE);

		String json = """
		{
		    "email": "%s",
		    "password": "Wrong123"
		}
		""".formatted(email);

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Invalid email or password."));
	}

	@Test
	void should_return_unauthorized_for_unknown_email() throws Exception {
		String json = """
		{
		    "email": "missing@test.com",
		    "password": "Secret123"
		}
		""";

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Invalid email or password."));
	}

	@Test
	void should_reject_invalid_request_body() throws Exception {
		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("not-json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Invalid request body."));
	}

	@Test
	void should_reject_validation_errors() throws Exception {
		String json = """
		{
		    "email": "",
		    "password": ""
		}
		""";

		mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.email").exists())
				.andExpect(jsonPath("$.password").exists());
	}
}