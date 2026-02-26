package com.example.acacia.service;

import com.example.acacia.dto.ExtraDto;
import com.example.acacia.dto.MetaData;
import com.example.acacia.dto.Response;
import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.enums.ResponseStatusEnum;
import com.example.acacia.repository.ExtraRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExtraService {
    private final ExtraRepository extraRepository;

    public Response<List<ExtraDto>> getExtras(Pageable pageable, ExtraType extraType) {
        try {
            Page<Tuple> extrasPage = extraRepository.findExtras(pageable, extraType);

            List<ExtraDto> dtoList = extrasPage.getContent().stream()
                    .map(tuple -> ExtraDto.builder()
                            .id(tuple.get(0, Long.class))
                            .memberName(tuple.get(1, String.class))
                            .amount(tuple.get(2, BigDecimal.class))
                            .date(tuple.get(3, LocalDate.class))
                            .periodDate(tuple.get(4, LocalDate.class))
                            .extraType(tuple.get(4, ExtraType.class))
                            .status(tuple.get(4, ExtraStatus.class))
                            .build())
                    .collect(Collectors.toList());

            MetaData metaData = MetaData.builder()
                    .page(extrasPage.getNumber())
                    .totalElements(extrasPage.getTotalElements())
                    .totalPages(extrasPage.getTotalPages())
                    .limit(extrasPage.getSize())
                    .build();

            return Response.<List<ExtraDto>>builder()
                    .status(ResponseStatusEnum.SUCCESS)
                    .data(dtoList)
                    .message("Extras retrieved successfully")
                    .metaData(metaData)
                    .build();

        } catch (Exception e) {
            // It's good practice to log the error here
            throw new RuntimeException("Error fetching extras: " + e.getMessage());
        }
    }
}
