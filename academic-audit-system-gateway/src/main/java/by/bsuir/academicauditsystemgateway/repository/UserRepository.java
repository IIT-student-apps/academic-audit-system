package by.bsuir.academicauditsystemgateway.repository;

import by.bsuir.academicauditsystemgateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
