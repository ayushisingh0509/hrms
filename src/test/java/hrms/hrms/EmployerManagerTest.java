package hrms.hrms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import hrms.hrms.business.abstracts.EmployerService;
import hrms.hrms.dto.request.EmployerRegisterRequest;
import hrms.hrms.entity.Employer;
import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.EmployerDao;
import hrms.hrms.repository.UserDao;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmployerManagerTest {

	@Autowired
	private EmployerService employerService;

	@Autowired
	private EmployerDao employerDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void register_should_store_encoded_password_in_user() {
		EmployerRegisterRequest request = new EmployerRegisterRequest(
				"Test Company",
				"https://testcompany.com",
				"employer@test.com",
				"05001234567",
				"RawPass123",
				"RawPass123");

		employerService.register(request);

		Employer employer = employerDao.findByUser_Email("employer@test.com").orElseThrow();
		User user = userDao.findById(employer.getUser().getId()).orElseThrow();

		assertThat(user.getPassword()).isNotEqualTo("RawPass123");
		assertThat(passwordEncoder.matches("RawPass123", user.getPassword())).isTrue();
		assertThat(user.getRole()).isEqualTo(Role.EMPLOYER);
	}
}