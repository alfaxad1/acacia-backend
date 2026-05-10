package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.FineTypeDto;
import com.example.acacia.dto.FineTypeRequest;
import com.example.acacia.model.FineType;
import com.example.acacia.repository.FineTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FineTypeService {

    private final FineTypeRepository fineTypeRepository;

    public FineTypeDto createFineType(FineTypeRequest request) {
        FineType fineType = new FineType();
        fineType.setName(request.getName());
        fineType.setDescription(request.getDescription());
        fineType.setAmount(request.getAmount());
        fineType.setPercentage(request.getPercentage());

        FineType saved = fineTypeRepository.save(fineType);
        return mapToDto(saved);
    }

    public List<FineTypeDto> getAllFineTypes() {
        return fineTypeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public FineTypeDto getFineTypeById(Long id) {
        FineType fineType = fineTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine type not found with id " + id));
        return mapToDto(fineType);
    }

    public FineTypeDto updateFineType(Long id, FineTypeRequest request) {
        FineType fineType = fineTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine type not found with id " + id));

        fineType.setName(request.getName());
        fineType.setDescription(request.getDescription());
        fineType.setAmount(request.getAmount());
        fineType.setPercentage(request.getPercentage());

        FineType updated = fineTypeRepository.save(fineType);
        return mapToDto(updated);
    }

    public void deleteFineType(Long id) {
        FineType fineType = fineTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine type not found with id " + id));
        fineTypeRepository.delete(fineType);
    }

    private FineTypeDto mapToDto(FineType fineType) {
        return FineTypeDto.builder()
                .id(fineType.getId())
                .name(fineType.getName())
                .description(fineType.getDescription())
                .amount(fineType.getAmount())
                .percentage(fineType.getPercentage())
                .build();
    }
}
