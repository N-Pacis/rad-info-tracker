package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.PatientAppointment;
import rw.auca.radinfotracker.model.UserAccount;
import rw.auca.radinfotracker.model.enums.EAppointmentStatus;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IInsuranceRepository extends JpaRepository<Insurance, UUID> {

    @Query("SELECT i FROM Insurance i WHERE ((:status IS NULL OR i.status =: status) AND (:query IS NULL OR (LOWER(TRIM(i.name)) LIKE LOWER(CONCAT('%', :query, '%')))))")
    Page<Insurance> searchAll(String query, EInsuranceStatus status, Pageable pageable);

    @Query("SELECT i FROM Insurance i WHERE ((:status IS NULL OR i.status =: status) AND (:query IS NULL OR (LOWER(TRIM(i.name)) LIKE LOWER(CONCAT('%', :query, '%')))))")
    List<Insurance> searchAll(String query, EInsuranceStatus status);

    Optional<Insurance> findByNameIgnoreCase(String name);
}
