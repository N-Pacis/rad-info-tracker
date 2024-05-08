package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
import rw.auca.radinfotracker.model.enums.*;
import rw.auca.radinfotracker.repository.IImageTypeRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageTypeServiceImplTest {

    @Mock private IImageTypeRepository imageTypeRepository;

    private final Faker faker = new Faker();

    private ImageTypeServiceImpl imageTypeService;

    @BeforeEach
    void setUp() {
        imageTypeService = new ImageTypeServiceImpl(imageTypeRepository);
    }

    @Test
    void register_WithUniqueImageTypeName_CreatesNewImageType() throws BadRequestException {
        NewImageTypeDTO newImageTypeDTO = new NewImageTypeDTO(faker.medical().medicineName(), Double.valueOf(faker.commerce().price()));
        when(imageTypeRepository.findByNameIgnoreCase(newImageTypeDTO.getName())).thenReturn(Optional.empty());

       imageTypeService.register(newImageTypeDTO);

        verify(imageTypeRepository, times(1)).save(any(ImageType.class));
    }

    @Test
    void register_WithDuplicateImageTypeName_ThrowsBadRequestException() {
        NewImageTypeDTO newImageTypeDTO = new NewImageTypeDTO(faker.medical().medicineName(), Double.valueOf(faker.commerce().price()));
        ImageType existingImageType = createImageType();
        existingImageType.setName(newImageTypeDTO.getName());
        when(imageTypeRepository.findByNameIgnoreCase(newImageTypeDTO.getName())).thenReturn(Optional.of(existingImageType));

        assertThrows(BadRequestException.class, () -> imageTypeService.register(newImageTypeDTO));
        verify(imageTypeRepository, never()).save(any(ImageType.class));
    }

    @Test
    void getById_WithValidId_ReturnsImageType() throws ResourceNotFoundException {
        ImageType imageType = createImageType();
        when(imageTypeRepository.findById(imageType.getId())).thenReturn(Optional.of(imageType));

        ImageType foundImageType = imageTypeService.getById(imageType.getId());

        assertEquals(imageType, foundImageType);
    }

    @Test
    void getById_WithInvalidId_ThrowsResourceNotFoundException() {
        UUID invalidId = UUID.randomUUID();
        when(imageTypeRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> imageTypeService.getById(invalidId));
    }

    private ImageType createImageType(){
        return new ImageType(UUID.randomUUID(), faker.medical().medicineName(), EImageTypeStatus.ACTIVE, Double.valueOf(faker.commerce().price()));
    }

}