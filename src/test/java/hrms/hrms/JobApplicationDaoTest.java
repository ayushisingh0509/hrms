package hrms.hrms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import hrms.hrms.entity.City;
import hrms.hrms.entity.Employer;
import hrms.hrms.entity.JobAdvertisement;
import hrms.hrms.entity.JobApplication;
import hrms.hrms.entity.JobApplicationStatus;
import hrms.hrms.entity.JobPosition;
import hrms.hrms.entity.JobSeeker;
import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.CityDao;
import hrms.hrms.repository.EmployerDao;
import hrms.hrms.repository.JobAdvertisementDao;
import hrms.hrms.repository.JobApplicationDao;
import hrms.hrms.repository.JobPositionDao;
import hrms.hrms.repository.JobSeekerDao;
import hrms.hrms.repository.UserDao;
import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class JobApplicationDaoTest {

	@Autowired
	private JobApplicationDao jobApplicationDao;

	@Autowired
	private JobAdvertisementDao jobAdvertisementDao;

	@Autowired
	private JobSeekerDao jobSeekerDao;

	@Autowired
	private JobPositionDao jobPositionDao;

	@Autowired
	private CityDao cityDao;

	@Autowired
	private EmployerDao employerDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User createUser(String email, Role role) {
		User user = new User();
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode("123456"));
		user.setRole(role);
		return userDao.save(user);
	}

	private Employer createEmployer() {
		Employer employer = new Employer();
		employer.setCompanyName("ABC Company");
		employer.setCompanyWebPage("https://abc.com");
		employer.setPhoneNumber("5551234567");
		employer.setUser(createUser("abc@test.com", Role.EMPLOYER));
		return employerDao.save(employer);
	}

	private JobAdvertisement createAdvertisement() {
		JobPosition position = jobPositionDao.save(new JobPosition(null, "Java Dev", null));
		City city = cityDao.save(new City(null, "Istanbul", null));
		Employer employer = createEmployer();

		JobAdvertisement ad = new JobAdvertisement();
		ad.setOpenPositionCount(3);
		ad.setDescription("Backend Developer");
		ad.setMinSalary(10000);
		ad.setMaxSalary(20000);
		ad.setApplicationDeadline(LocalDate.now().plusDays(10));
		ad.setJobPosition(position);
		ad.setCity(city);
		ad.setEmployer(employer);

		return jobAdvertisementDao.save(ad);
	}

	private JobSeeker createJobSeeker() {
	    JobSeeker seeker = new JobSeeker();

	    seeker.setName("Aysu");
	    seeker.setLastName("Ay");
	    seeker.setNationalId("12345678901");
	    seeker.setBirthDate(LocalDate.of(2000, 6, 6));
	    seeker.setUser(createUser("aysu@test.com", Role.CANDIDATE));

	    return jobSeekerDao.save(seeker);
	}
	
	@Test
	void should_create_job_application_successfully() {

		JobAdvertisement ad = createAdvertisement();
		JobSeeker seeker = createJobSeeker();

		JobApplication app = new JobApplication();
		app.setJobAdvertisement(ad);
		app.setJobSeeker(seeker);

		JobApplication saved = jobApplicationDao.save(app);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getStatus()).isEqualTo(JobApplicationStatus.PENDING);
	}

	@Test
	void should_link_application_to_job_and_seeker() {

		JobAdvertisement ad = createAdvertisement();
		JobSeeker seeker = createJobSeeker();

		JobApplication app = new JobApplication();
		app.setJobAdvertisement(ad);
		app.setJobSeeker(seeker);

		JobApplication saved = jobApplicationDao.save(app);

		assertThat(saved.getJobAdvertisement().getId()).isEqualTo(ad.getId());
		assertThat(saved.getJobSeeker().getId()).isEqualTo(seeker.getId());
	}

	@Test
	void should_prevent_duplicate_application() {

		JobAdvertisement ad = createAdvertisement();
		JobSeeker seeker = createJobSeeker();

		JobApplication app1 = new JobApplication();
		app1.setJobAdvertisement(ad);
		app1.setJobSeeker(seeker);

		jobApplicationDao.saveAndFlush(app1);

		JobApplication app2 = new JobApplication();
		app2.setJobAdvertisement(ad);
		app2.setJobSeeker(seeker);

		assertThatThrownBy(() -> jobApplicationDao.saveAndFlush(app2));
	}
}
