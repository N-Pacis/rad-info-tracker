package rw.auca.radinfotracker.repository;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.model.*;
import rw.auca.radinfotracker.model.enums.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IInsuranceRepositoryTest {

    @Autowired
    private IInsuranceRepository insuranceRepository;

    private final Faker faker = new Faker();

    @AfterEach
    void tearDown() {
        insuranceRepository.deleteAll();
    }

    @Test
    void itShouldReturnAllInsurancesPaginated() {
        Pageable pageable = PageRequest.of(0, 10);

        Insurance insurance = createInsurance();

        Page<Insurance> insurances = insuranceRepository.searchAll(null,null, pageable);

        assertEquals(insurances.getTotalElements(), 1);
        assertThat(insurances.getContent()).contains(insurance);
    }


    @Test
    void itShouldReturnAllInsurancesByFiltersPaginated() {
        Pageable pageable = PageRequest.of(0, 10);

        Insurance insurance = createInsurance();

        Insurance secondInsurance = createInsurance();

        Page<Insurance> insurances = insuranceRepository.searchAll(insurance.getName(),insurance.getStatus(), pageable);

        assertEquals(insurances.getTotalElements(), 1);
        assertThat(insurances.getContent()).contains(insurance);
    }

    @Test
    void itShouldReturnAllInsurancesListed() {
        Insurance insurance = createInsurance();

        List<Insurance> insurances = insuranceRepository.searchAll(null,null);

        assertEquals(insurances.size(), 1);
        assertThat(insurances).contains(insurance);
    }


    @Test
    void itShouldReturnAllInsurancesByFiltersListed() {

        Insurance insurance = createInsurance();

        Insurance secondInsurance = createInsurance();

        List<Insurance> insurances = insuranceRepository.searchAll(insurance.getName(),insurance.getStatus());

        assertEquals(insurances.size(), 1);
        assertThat(insurances).contains(insurance);
    }

    @Test
    void findByNameIgnoreCase() {
        Insurance insuranceToSave = createInsurance();

        Optional<Insurance> insurance = insuranceRepository.findByNameIgnoreCase(insuranceToSave.getName());

        assertTrue(insurance.isPresent());
        assertEquals(insurance.get().getName(), insuranceToSave.getName());
    }


    private Insurance createInsurance(){
        Insurance insurance = new Insurance(UUID.randomUUID(), faker.company().name(), faker.number().randomDouble(2, 0,1), EInsuranceStatus.ACTIVE);
        return insuranceRepository.save(insurance);
    }
}