package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.FineDto;
import com.example.acacia.dto.FineRequest;
import com.example.acacia.dto.LoanDto;
import com.example.acacia.enums.FineStatus;
import com.example.acacia.enums.FineTyp;
import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.Fine;
import com.example.acacia.model.Member;
import com.example.acacia.model.SaccoSetups;
import com.example.acacia.repository.FineRepository;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {
    private final FineRepository fineRepository;
    private final SaccoSetupRepository setupRepository;
    private final MemberRepository memberRepository;

    public void recordFine(FineRequest fineRequest) {
        try{
            SaccoSetups setups = setupRepository.findByStatus(SetupStatus.ACTIVE);
            BigDecimal amount = fineRequest.getAmount();
            if(fineRequest.getType().equals(FineTyp.LATE_MEETINGS)){
                amount = setups.getMeetingLateFineAmount();
            } else if (fineRequest.getType().equals(FineTyp.MEETING_ABSENTEEISM)) {
                amount = setups.getMeetingAbsentFineAmount();
            }
            Member member = memberRepository.findById(fineRequest.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member doesn't exist"));

            Fine fine = Fine.builder()
                    .member(member)
                    .amount(amount)
                    .type(fineRequest.getType())
                    .fineDate(fineRequest.getFineDate())
                    .status(FineStatus.UNPAID)
                    .build();
            fineRepository.save(fine);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void settleFine(Long fineId) {
        try {
            Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ResourceNotFoundException("Fine doesn't exist"));
            fine.setStatus(FineStatus.PAID);
            fine.setPaid(true);
            fine.setPaidDate(LocalDate.now());

            fineRepository.save(fine);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<FineDto> getFines(FineStatus status) {
        try{
            List<FineDto> finesList = new ArrayList<>();
            List<Tuple> fines = fineRepository.findFines(status);
            for (Tuple fine : fines) {
                FineDto dto = FineDto.builder()
                        .id(fine.get(0, Long.class))
                        .memberName(fine.get(1, String.class))
                        .amount(fine.get(2, BigDecimal.class))
                        .date(fine.get(3, LocalDate.class))
                        .status(fine.get(4, FineStatus.class))
                        .type(fine.get(5, FineTyp.class))
                        .paidDate(fine.get(6, LocalDate.class))
                        .build();
                finesList.add(dto);
            }
            return finesList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
