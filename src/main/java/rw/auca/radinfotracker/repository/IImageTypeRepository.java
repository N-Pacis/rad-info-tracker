package rw.auca.radinfotracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rw.auca.radinfotracker.model.ImageType;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IImageTypeRepository extends JpaRepository<ImageType, UUID> {

    @Query("SELECT i FROM ImageType i WHERE ((:status IS NULL OR i.status =: status) AND (:query IS NULL OR (LOWER(TRIM(i.name)) LIKE LOWER(CONCAT('%', :query, '%')))))")
    Page<ImageType> searchAll(String query, EImageTypeStatus status, Pageable pageable);

    @Query("SELECT i FROM ImageType i WHERE ((:status IS NULL OR i.status =: status) AND (:query IS NULL OR (LOWER(TRIM(i.name)) LIKE LOWER(CONCAT('%', :query, '%')))))")
    List<ImageType> searchAll(String query, EImageTypeStatus status);

    Optional<ImageType> findByNameIgnoreCase(String name);
}
