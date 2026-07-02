package hrms.hrms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hrms.hrms.entity.Employer;

public interface EmployerDao extends JpaRepository<Employer, Integer> {

	java.util.Optional<Employer> findByUser_Email(String email);
	

}
