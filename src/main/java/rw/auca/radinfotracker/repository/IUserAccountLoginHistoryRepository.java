package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountLoginHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IUserAccountLoginHistoryRepository extends JpaRepository<UserAccountLoginHistory, UUID> {

    Integer countAllByUserAndUserAgent(UserAccount user, String userAgent);

    Page<UserAccountLoginHistory> findByUserAndCreatedAtBetween(UserAccount user, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

}
