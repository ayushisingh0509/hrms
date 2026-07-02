package hrms.hrms;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import hrms.hrms.business.abstracts.JobSeekerService;
import hrms.hrms.dto.request.JobSeekerRegisterRequest;
import hrms.hrms.entity.JobSeeker;
import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.JobSeekerDao;
import hrms.hrms.repository.UserDao;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class JobSeekerManagerTest {

	@Autowired
	private JobSeekerService jobSeekerService;

	@Autowired
	private JobSeekerDao jobSeekerDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void register_should_store_encoded_password_in_user() {
		JobSeekerRegisterRequest request = new JobSeekerRegisterRequest(
				"Aysu",
				"Ay",
				"12345678901",
				LocalDate.of(2000, 6, 6),
				"candidate@test.com",
				"RawPass123",
				"RawPass123");

		jobSeekerService.register(request);

		JobSeeker jobSeeker = jobSeekerDao.findByUser_Email("candidate@test.com").orElseThrow();
		User user = userDao.findById(jobSeeker.getUser().getId()).orElseThrow();

		assertThat(user.getPassword()).isNotEqualTo("RawPass123");
		assertThat(passwordEncoder.matches("RawPass123", user.getPassword())).isTrue();
		assertThat(user.getRole()).isEqualTo(Role.CANDIDATE);
	}
}