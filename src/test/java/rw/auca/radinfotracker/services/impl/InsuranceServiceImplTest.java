package rw.auca.radinfotracker.services.impl;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;
import rw.auca.radinfotracker.repository.IInsuranceRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceServiceImplTest {

    @Mock private IInsuranceRepository insuranceRepository;

    private InsuranceServiceImpl insuranceService;

    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        insuranceService = new InsuranceServiceImpl(insuranceRepository);
    }

    @Test
    void register_WithUniqueInsuranceName_CreatesNewInsurance() throws BadRequestException {
        NewInsuranceDTO newInsuranceDTO = new NewInsuranceDTO(faker.company().name(), faker.number().randomDouble(2, 0,1));
        when(insuranceRepository.findByNameIgnoreCase(newInsuranceDTO.getName())).thenReturn(Optional.empty());

        insuranceService.register(newInsuranceDTO);

        verify(insuranceRepository, times(1)).save(any(Insurance.class));
    }

    @Test
    void register_WithDuplicateInsuranceName_ThrowsBadRequestException() {
        NewInsuranceDTO newInsuranceDTO = new NewInsuranceDTO(faker.company().name(), faker.number().randomDouble(2, 0,1));
        Insurance existingInsurance = createInsurance();
        existingInsurance.setName(newInsuranceDTO.getName());
        when(insuranceRepository.findByNameIgnoreCase(newInsuranceDTO.getName())).thenReturn(Optional.of(existingInsurance));

        assertThrows(BadRequestException.class, () -> insuranceService.register(newInsuranceDTO));
        verify(insuranceRepository, never()).save(any(Insurance.class));
    }

    @Test
    void register_WithInvalidRate_ThrowsBadRequestException() {
        NewInsuranceDTO newInsuranceDTO = new NewInsuranceDTO(faker.company().name(), -0.5);

        assertThrows(BadRequestException.class, () -> insuranceService.register(newInsuranceDTO));
        verify(insuranceRepository, never()).save(any(Insurance.class));
    }

    @Test
    void getById_WithValidId_ReturnsInsurance() throws ResourceNotFoundException {
        Insurance insurance = createInsurance();
        when(insuranceRepository.findById(insurance.getId())).thenReturn(Optional.of(insurance));

        Insurance foundInsurance = insuranceService.getById(insurance.getId());

        assertEquals(insurance, foundInsurance);
    }

    @Test
    void getById_WithInvalidId_ThrowsResourceNotFoundException() {
        UUID invalidId = UUID.randomUUID();
        when(insuranceRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> insuranceService.getById(invalidId));
    }

    private Insurance createInsurance(){
        return new Insurance(UUID.randomUUID(), faker.company().name(), faker.number().randomDouble(2, 0,1), EInsuranceStatus.ACTIVE);
    }
}