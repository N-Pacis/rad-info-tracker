package rw.auca.radinfotracker.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class IInsuranceRepositoryTest {

    @Autowired
    private IInsuranceRepository insuranceRepository;

    @AfterEach
    void tearDown() {
        insuranceRepository.deleteAll();
    }

    @Test
    void itShouldReturnAllInsurancesPaginated() {
        String name = "Insurance name";
        EInsuranceStatus status = EInsuranceStatus.ACTIVE;
        Double rate = 0.8;
        Pageable pageable = PageRequest.of(0, 10);

        Insurance insurance = new Insurance(name, rate, status);
        insuranceRepository.save(insurance);

        Page<Insurance> insurances = insuranceRepository.searchAll(null,null, pageable);

        assertEquals(insurances.getTotalElements(), 1);
        assertThat(insurances.getContent()).contains(insurance);
    }


    @Test
    void itShouldReturnAllInsurancesByFiltersPaginated() {
        String name = "Insurance name";
        EInsuranceStatus status = EInsuranceStatus.ACTIVE;
        Double rate = 0.8;

        String secondName = "Second";
        EInsuranceStatus secondStatus = EInsuranceStatus.ACTIVE;
        Double secondRate = 0.8;

        Pageable pageable = PageRequest.of(0, 10);

        Insurance insurance = new Insurance(name, rate, status);
        insuranceRepository.save(insurance);

        Insurance secondInsurance = new Insurance(secondName, secondRate, secondStatus);
        insuranceRepository.save(secondInsurance);

        Page<Insurance> insurances = insuranceRepository.searchAll(name,status, pageable);

        assertEquals(insurances.getTotalElements(), 1);
        assertThat(insurances.getContent()).contains(insurance);
    }

    @Test
    void itShouldReturnAllInsurancesListed() {
        String name = "Insurance name";
        EInsuranceStatus status = EInsuranceStatus.ACTIVE;
        Double rate = 0.8;

        Insurance insurance = new Insurance(name, rate, status);
        insuranceRepository.save(insurance);

        List<Insurance> insurances = insuranceRepository.searchAll(null,null);

        assertEquals(insurances.size(), 1);
        assertThat(insurances).contains(insurance);
    }


    @Test
    void itShouldReturnAllInsurancesByFiltersListed() {
        String name = "Insurance name";
        EInsuranceStatus status = EInsuranceStatus.ACTIVE;
        Double rate = 0.8;

        String secondName = "Second";
        EInsuranceStatus secondStatus = EInsuranceStatus.INACTIVE;
        Double secondRate = 0.8;

        Insurance insurance = new Insurance(name, rate, status);
        insuranceRepository.save(insurance);

        Insurance secondInsurance = new Insurance(secondName, secondRate, secondStatus);
        insuranceRepository.save(secondInsurance);

        List<Insurance> insurances = insuranceRepository.searchAll(name,status);

        assertEquals(insurances.size(), 1);
        assertThat(insurances).contains(insurance);
    }

    @Test
    void findByNameIgnoreCase() {
        String name = "Insurance name";
        EInsuranceStatus status = EInsuranceStatus.ACTIVE;
        Double rate = 0.8;

        Insurance insuranceToSave = new Insurance(name, rate, status);
        insuranceRepository.save(insuranceToSave);

        Optional<Insurance> insurance = insuranceRepository.findByNameIgnoreCase(name);

        assertTrue(insurance.isPresent());
        assertEquals(insurance.get().getName(), name);
    }

}