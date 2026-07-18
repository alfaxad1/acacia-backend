package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.B2cTransferDto;
import com.example.acacia.enums.TransactionStatus;
import com.example.acacia.model.B2cTransferRequest;
import com.example.acacia.model.Member;
import com.example.acacia.repository.B2cTransferRequestRepository;
import com.example.acacia.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class B2cTransferService {
    private final B2cTransferRequestRepository b2cTransferRequestRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MpesaService mpesaService;

    public B2cTransferDto initiateTransfer(BigDecimal amount, String recipientPhone, String reason, String pin, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!passwordEncoder.matches(pin, member.getPassword())) {
            throw new IllegalArgumentException("Invalid PIN/Password");
        }

        B2cTransferRequest request = B2cTransferRequest.builder()
                .amount(amount)
                .recipientPhone(recipientPhone)
                .reason(reason)
                .initiatedBy(member)
                .status(TransactionStatus.PENDING)
                .build();

        B2cTransferRequest saved = b2cTransferRequestRepository.save(request);
        return mapToDto(saved);
    }

    public B2cTransferDto authorizeTransfer(Long requestId, Long memberId, boolean approve) throws Exception {
        B2cTransferRequest request = b2cTransferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found"));

        if (request.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transfer is not in PENDING state");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // Uncomment if we want to prevent the initiator from authorizing their own request
        // if (request.getInitiatedBy().getId().equals(memberId)) {
        //     throw new IllegalStateException("Initiator cannot authorize their own transfer");
        // }

        request.setAuthorizedBy(member);

        if (approve) {
            request.setStatus(TransactionStatus.APPROVED);
            mpesaService.sendGenericB2cFunds(request.getRecipientPhone(), request.getAmount(), request.getReason());
        } else {
            request.setStatus(TransactionStatus.FAILED);
        }

        B2cTransferRequest saved = b2cTransferRequestRepository.save(request);
        return mapToDto(saved);
    }

    public List<B2cTransferDto> getAllTransfers() {
        return b2cTransferRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private B2cTransferDto mapToDto(B2cTransferRequest request) {
        return B2cTransferDto.builder()
                .id(request.getId())
                .amount(request.getAmount())
                .recipientPhone(request.getRecipientPhone())
                .reason(request.getReason())
                .status(request.getStatus())
                .initiatedById(request.getInitiatedBy() != null ? request.getInitiatedBy().getId() : null)
                .initiatedByName(request.getInitiatedBy() != null ? request.getInitiatedBy().getFullName() : null)
                .authorizedById(request.getAuthorizedBy() != null ? request.getAuthorizedBy().getId() : null)
                .authorizedByName(request.getAuthorizedBy() != null ? request.getAuthorizedBy().getFullName() : null)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
