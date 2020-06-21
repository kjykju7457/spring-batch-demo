package batch.repository;


import batch.domain.User;
import batch.domain.enums.Grade;
import batch.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by youngjae on 2018. 3. 3..
 */
@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUpdatedDateBeforeAndStatusEquals(LocalDateTime localDateTime, UserStatus status);
    List<User> findByUpdatedDateBeforeAndStatusEqualsAndGradeEquals(LocalDateTime localDateTime, UserStatus status, Grade grade);
}
