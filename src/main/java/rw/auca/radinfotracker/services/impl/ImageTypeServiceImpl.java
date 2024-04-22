package rw.auca.radinfotracker.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.ImageType;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;
import rw.auca.radinfotracker.repository.IImageTypeRepository;
import rw.auca.radinfotracker.services.IImageTypeService;

import java.util.List;
import java.util.UUID;

@Service
public class ImageTypeServiceImpl implements IImageTypeService {

    private final IImageTypeRepository imageTypeRepository;

    public ImageTypeServiceImpl(IImageTypeRepository imageTypeRepository) {
        this.imageTypeRepository = imageTypeRepository;
    }

    @Override
    public ImageType register(NewImageTypeDTO dto) throws BadRequestException {
        if(imageTypeRepository.findByNameIgnoreCase(dto.getName()).isPresent()) throw new BadRequestException("exceptions.badRequest.imageType.nameExists");
        ImageType imageType = new ImageType(dto);
        return imageTypeRepository.save(imageType);
    }

    @Override
    public ImageType getById(UUID id) throws ResourceNotFoundException {
        return imageTypeRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("exceptions.notFound.imageType"));
    }

    @Override
    public Page<ImageType> getAll(String query, EImageTypeStatus status, Pageable pageable) {
        return imageTypeRepository.searchAll(query,status,pageable);
    }

    @Override
    public List<ImageType> getAll(String query) {
        return imageTypeRepository.searchAll(query,EImageTypeStatus.ACTIVE);
    }
}
