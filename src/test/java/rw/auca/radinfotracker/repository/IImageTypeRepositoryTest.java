package rw.auca.radinfotracker.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.model.ImageType;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class IImageTypeRepositoryTest {

    @Autowired
    private IImageTypeRepository imageTypeRepository;

    @AfterEach
    void tearDown() {
        imageTypeRepository.deleteAll();
    }

    @Test
    void itShouldReturnAllImageTypesPaginated() {
        String name = "Image Type";
        EImageTypeStatus status = EImageTypeStatus.ACTIVE;
        Double totalCost = 1000000.00;
        Pageable pageable = PageRequest.of(0, 10);

        ImageType imageType = new ImageType(name, status, totalCost);
        imageTypeRepository.save(imageType);

        Page<ImageType> imageTypes = imageTypeRepository.searchAll(null,null, pageable);

        assertEquals(imageTypes.getTotalElements(), 1);
        assertThat(imageTypes.getContent()).contains(imageType);
    }


    @Test
    void itShouldReturnAllImageTypesByFiltersPaginated() {
        String name = "Image Type";
        EImageTypeStatus status = EImageTypeStatus.ACTIVE;
        Double totalCost = 1000000.00;

        String secondName = "2nd Type";
        EImageTypeStatus secondStatus = EImageTypeStatus.INACTIVE;
        Double secondTotalCost = 1000000.00;

        Pageable pageable = PageRequest.of(0, 10);

        ImageType imageType = new ImageType(name, status, totalCost);
        imageTypeRepository.save(imageType);

        ImageType secondImageType = new ImageType(secondName, secondStatus, secondTotalCost);
        imageTypeRepository.save(secondImageType);

        Page<ImageType> imageTypes = imageTypeRepository.searchAll(name,status, pageable);

        assertEquals(imageTypes.getTotalElements(), 1);
        assertThat(imageTypes.getContent()).contains(imageType);
    }

    @Test
    void itShouldReturnAllImageTypesListed() {
        String name = "Image Type";
        EImageTypeStatus status = EImageTypeStatus.ACTIVE;
        Double totalCost = 1000000.00;

        ImageType imageType = new ImageType(name, status, totalCost);
        imageTypeRepository.save(imageType);

        List<ImageType> imageTypes = imageTypeRepository.searchAll(null,null);

        assertEquals(imageTypes.size(), 1);
        assertThat(imageTypes).contains(imageType);
    }


    @Test
    void itShouldReturnAllImageTypesByFiltersListed() {
        String name = "Image Type";
        EImageTypeStatus status = EImageTypeStatus.ACTIVE;
        Double totalCost = 1000000.00;

        String secondName = "2nd Type";
        EImageTypeStatus secondStatus = EImageTypeStatus.INACTIVE;
        Double secondTotalCost = 1000000.00;

        ImageType imageType = new ImageType(name, status, totalCost);
        imageTypeRepository.save(imageType);

        ImageType secondImageType = new ImageType(secondName, secondStatus, secondTotalCost);
        imageTypeRepository.save(secondImageType);

        List<ImageType> imageTypes = imageTypeRepository.searchAll(name,status);

        assertEquals(imageTypes.size(), 1);
        assertThat(imageTypes).contains(imageType);
    }

    @Test
    void findByNameIgnoreCase() {
        String name = "Image Type";
        EImageTypeStatus status = EImageTypeStatus.ACTIVE;
        Double totalCost = 1000000.00;

        ImageType imageType = new ImageType(name, status, totalCost);
        imageTypeRepository.save(imageType);

        Optional<ImageType> image = imageTypeRepository.findByNameIgnoreCase(name);

        assertTrue(image.isPresent());
        assertEquals(image.get().getName(), name);
    }
}