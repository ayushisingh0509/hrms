package hrms.hrms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import hrms.hrms.entity.Employer;
import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.EmployerDao;
import hrms.hrms.repository.UserDao;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmployerDaoTest {
	
	@Autowired
    private EmployerDao employerDao;

    @Autowired
    private UserDao userDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

    private User validUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.EMPLOYER);
        return user;
    }

    private Employer validEmployer(String email) {
        Employer e = new Employer();
        e.setCompanyName("Test Company");
        e.setCompanyWebPage("https://testcompany.com");
        e.setPhoneNumber("05001234567");
        e.setUser(userDao.save(validUser(email)));
        return e;
    }

    @Test
    void whenValidEmployer_thenSaved() {
        Employer saved = employerDao.save(validEmployer("test@test.com"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCompanyName()).isEqualTo("Test Company");
    }

    @Test
    void whenDuplicateEmail_thenThrowsException() {
        employerDao.save(validEmployer("duplicate@test.com"));
    	employerDao.flush();

        assertThrows(DataIntegrityViolationException.class, () -> {
        	employerDao.save(validEmployer("duplicate@test.com"));
            employerDao.flush();
        });
    }

}
