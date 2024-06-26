package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.ERole;
import rw.auca.radinfotracker.model.enums.EUserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByPhoneNumber(String phoneNumber);
    Optional<UserAccount> findByEmail(String email);

    @Query("SELECT u FROM UserAccount u WHERE " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:role IS NULL OR u.role = :role) AND (" +
            "  (LOWER(CONCAT(TRIM(u.firstName), ' ', TRIM(u.lastName))) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
            "  (LOWER(TRIM(u.email)) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
            "  (LOWER(TRIM(u.phoneNumber)) LIKE LOWER(CONCAT('%', :query, '%')))" +
            ")")
    Page<UserAccount> searchAll(String query, EUserStatus status, ERole role, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:role IS NULL OR u.role = :role) AND (" +
            "  (LOWER(CONCAT(TRIM(u.firstName), ' ', TRIM(u.lastName))) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
            "  (LOWER(TRIM(u.email)) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
            "  (LOWER(TRIM(u.phoneNumber)) LIKE LOWER(CONCAT('%', :query, '%')))" +
            ")")
    List<UserAccount> searchAll(String query, EUserStatus status, ERole role);

    Optional<UserAccount> findBySessionIdAndId(UUID sessionId, UUID userId);


}
