package rw.auca.radinfotracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.UserAccountAudit;

import java.util.List;
import java.util.UUID;

@Repository
public interface IUserAccountAuditRepository extends JpaRepository<UserAccountAudit, UUID> {
    List<UserAccountAudit> findAllByUserAccount(UserAccount userAccount);
}
