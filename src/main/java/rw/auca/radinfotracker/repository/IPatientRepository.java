package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.Patient;
import rw.auca.radinfotracker.model.enums.EPatientStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPatientRepository extends JpaRepository<Patient, UUID> {

    @Query("SELECT p FROM Patient p WHERE " +
            "(:status IS NULL OR p.status = :status) AND (" +
            "LOWER(CONCAT(TRIM(p.firstName), ' ', TRIM(p.lastName))) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(TRIM(p.firstName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(TRIM(p.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(TRIM(p.refNumber)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(TRIM(p.address)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(TRIM(p.phoneNumber)) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Patient> searchAll(String query, EPatientStatus status, Pageable pageable);

    Optional<Patient> findByRefNumber(String refNumber);
}
