package rw.auca.radinfotracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;

import java.util.List;
import java.util.UUID;

@Repository
public interface IUserAccountLoginHistoryRepository extends JpaRepository<UserAccountLoginHistory, UUID> {

    Integer countAllByUserAndUserAgent(UserAccount user, String userAgent);

    List<UserAccountLoginHistory> findByUserOrderByCreatedAtDesc(UserAccount user);
}
