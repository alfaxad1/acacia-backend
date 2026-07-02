package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.FineDto;
import com.example.acacia.dto.FineRequest;
import com.example.acacia.dto.StkPushResponse;
import com.example.acacia.enums.*;
import com.example.acacia.model.Fine;
import com.example.acacia.model.Member;
import com.example.acacia.model.SaccoSetups;
import com.example.acacia.model.Transaction;
import com.example.acacia.model.FineType;
import com.example.acacia.repository.FineRepository;
import com.example.acacia.repository.FineTypeRepository;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import com.example.acacia.repository.TransactionRepository;
import com.example.acacia.utility.FormatPhone;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {
    private final FineRepository fineRepository;
    private final FineTypeRepository fineTypeRepository;
    private final SaccoSetupRepository setupRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final MpesaService mpesaService;
    private final TransactionRepository transactionRepository;
    private final FormatPhone formatPhone;

    private static final Logger logger = LoggerFactory.getLogger(FineService.class);

    public void recordFine(FineRequest fineRequest) {
        try{
            Member member = memberRepository.findById(fineRequest.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member doesn't exist"));

            FineType fineType = fineTypeRepository.findById(fineRequest.getFineTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("FineType doesn't exist"));

            BigDecimal amount = fineType.getAmount();

            Fine fine = Fine.builder()
                    .member(member)
                    .amount(amount)
                    .type(fineType)
                    .fineDate(fineRequest.getFineDate())
                    .status(FineStatus.UNPAID)
                    .build();
            fineRepository.save(fine);

            emailService.sendMail(member.getEmail(), "FINE RECORD", "A fine has been recorded for you because of "+ fineType.getName());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StkPushResponse initiateFinePayment(Long fineId, String phone) throws IOException {
        Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ResourceNotFoundException("Fine doesn't exist"));
        logger.info("FIne found: {}", fine.getId());

        Member member = fine.getMember();

        String phoneNumber = (phone != null && !phone.trim().isEmpty()) ? phone : member.getPhone();

        logger.info("====Attempting stk push====");
        StkPushResponse mpesaResponse = mpesaService.stkPush(
                formatPhone.formatPhoneNumber(phoneNumber),
                fine.getAmount().toString(),
                "FINE-" + member.getMemberNumber(),
                fine.getType().getName()+"_FINE"
        );

        Transaction txn = new Transaction();
        txn.setCheckoutRequestID(mpesaResponse.getCheckoutRequestID());
        txn.setMember(member);
        txn.setAmount(fine.getAmount());
        txn.setFine(fine);
        txn.setType(TransactionType.FINE);
        txn.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(txn);

        return mpesaResponse;
    }

    public void settleFine(Fine fine) {
        try {
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
                        .fineTypeId(fine.get(5, Long.class))
                        .fineTypeName(fine.get(6, String.class))
                        .paidDate(fine.get(7, LocalDate.class))
                        .memberId(fine.get(8, Long.class))
                        .build();
                finesList.add(dto);
            }
            return finesList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
