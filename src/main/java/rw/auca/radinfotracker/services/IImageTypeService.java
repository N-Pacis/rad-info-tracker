package rw.auca.radinfotracker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.ImageType;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;

import java.util.List;
import java.util.UUID;

public interface IImageTypeService {

    ImageType register(NewImageTypeDTO dto) throws BadRequestException;

    ImageType getById(UUID id) throws ResourceNotFoundException;

    Page<ImageType> getAll(String query, EImageTypeStatus status, Pageable pageable);

    List<ImageType> getAll(String query);
}
