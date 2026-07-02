package hrms.hrms.business.concretes;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hrms.hrms.business.abstracts.EmployerService;
import hrms.hrms.core.utilities.DataResult;
import hrms.hrms.core.utilities.ErrorResult;
import hrms.hrms.core.utilities.Result;
import hrms.hrms.core.utilities.SuccessDataResult;
import hrms.hrms.core.utilities.SuccessResult;
import hrms.hrms.dto.EmployerDto;
import hrms.hrms.dto.request.EmployerRegisterRequest;
import hrms.hrms.entity.Employer;
import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.EmployerDao;
import hrms.hrms.repository.UserDao;

@Service
public class EmployerManager implements EmployerService {

	private final EmployerDao employerDao;
	private final UserDao userDao;
	private final PasswordEncoder passwordEncoder;

	public EmployerManager(EmployerDao employerDao, UserDao userDao, PasswordEncoder passwordEncoder) {
		this.employerDao = employerDao;
		this.userDao = userDao;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Result register(EmployerRegisterRequest request) {
		if (!request.getPassword().equals(request.getConfirmPassword())) {
			return new ErrorResult("Passwords do not match.");
		}
		if (userDao.existsByEmail(request.getEmail())) {
			return new ErrorResult("Email is already in use.");
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.EMPLOYER);
		user.setEnabled(true);
		user.setAccountNonLocked(true);
		user.setAccountNonExpired(true);
		user.setCredentialsNonExpired(true);
		userDao.save(user);

		Employer e = new Employer();
		e.setCompanyName(request.getCompanyName());
		e.setCompanyWebPage(request.getCompanyWebPage());
		e.setPhoneNumber(request.getPhoneNumber());
		e.setUser(user);
		employerDao.save(e);

		return new SuccessResult("Employer registered.");
	}

	@Override
	public DataResult<List<EmployerDto>> getAll() {
		var list = employerDao.findAll().stream().map(e -> new EmployerDto(e.getId(), e.getCompanyName(),
				e.getCompanyWebPage(), e.getUser().getEmail(), e.getPhoneNumber())).toList();
		return new SuccessDataResult<>(list, "Employers listed.");
	}
}
