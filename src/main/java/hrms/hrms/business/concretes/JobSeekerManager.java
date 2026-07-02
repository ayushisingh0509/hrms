package hrms.hrms.business.concretes;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hrms.hrms.business.abstracts.JobSeekerService;
import hrms.hrms.core.utilities.DataResult;
import hrms.hrms.core.utilities.ErrorResult;
import hrms.hrms.core.utilities.Result;
import hrms.hrms.core.utilities.SuccessDataResult;
import hrms.hrms.core.utilities.SuccessResult;
import hrms.hrms.dto.JobSeekerDto;
import hrms.hrms.dto.request.JobSeekerRegisterRequest;
import hrms.hrms.entity.JobSeeker;
import hrms.hrms.entity.Role;
import hrms.hrms.entity.User;
import hrms.hrms.repository.JobSeekerDao;
import hrms.hrms.repository.UserDao;

@Service
public class JobSeekerManager implements JobSeekerService {

	private final JobSeekerDao jobSeekerDao;
	private final UserDao userDao;
	private final PasswordEncoder passwordEncoder;

	public JobSeekerManager(JobSeekerDao jobSeekerDao, UserDao userDao, PasswordEncoder passwordEncoder) {
		this.jobSeekerDao = jobSeekerDao;
		this.userDao = userDao;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Result register(JobSeekerRegisterRequest request) {
		if (!request.getPassword().equals(request.getConfirmPassword())) {
			return new ErrorResult("Passwords do not match.");
		}
		if (userDao.existsByEmail(request.getEmail())) {
			return new ErrorResult("Email is already in use.");
		}
		if (jobSeekerDao.findByNationalId(request.getNationalId()).isPresent()) {
			return new ErrorResult("National ID is already in use.");
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.CANDIDATE);
		user.setEnabled(true);
		user.setAccountNonLocked(true);
		user.setAccountNonExpired(true);
		user.setCredentialsNonExpired(true);
		userDao.save(user);

		JobSeeker js = new JobSeeker();
		js.setName(request.getName());
		js.setLastName(request.getLastName());
		js.setNationalId(request.getNationalId());
		js.setBirthDate(request.getBirthDate());
		js.setUser(user);
		jobSeekerDao.save(js);

		return new SuccessResult("Job seeker registered.");
	}

	@Override
	public DataResult<List<JobSeekerDto>> getAll() {
		var list = jobSeekerDao.findAll().stream().map(j -> new JobSeekerDto(j.getId(), j.getName(), j.getLastName(),
				j.getNationalId(), j.getBirthDate(), j.getUser().getEmail())).toList();
		return new SuccessDataResult<>(list, "Job seekers listed.");
	}

}
