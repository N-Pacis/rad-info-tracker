package rw.auca.radinfotracker.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.Insurance;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;
import rw.auca.radinfotracker.repository.IInsuranceRepository;
import rw.auca.radinfotracker.services.IInsuranceService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements IInsuranceService {

    private final IInsuranceRepository insuranceRepository;

    @Override
    public Insurance register(NewInsuranceDTO dto) throws BadRequestException {
        if(insuranceRepository.findByNameIgnoreCase(dto.getName()).isPresent()) throw new BadRequestException("exceptions.badRequest.insurance.nameExists");
        if(dto.getRate() < 0.01 || dto.getRate() > 0.01) throw new BadRequestException("exceptions.badRequest.insurance.invalidRate");

        Insurance insurance = new Insurance(dto);
        return insuranceRepository.save(insurance);
    }

    @Override
    public Insurance getById(UUID id) throws ResourceNotFoundException {
        return insuranceRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("exceptions.notFound.insurance"));
    }

    @Override
    public Page<Insurance> getAll(String query, EInsuranceStatus status, Pageable pageable) {
        return insuranceRepository.searchAll(query,status,pageable);
    }

    @Override
    public List<Insurance> getAll(String query) {
        return insuranceRepository.searchAll(query, EInsuranceStatus.ACTIVE);
    }

}
