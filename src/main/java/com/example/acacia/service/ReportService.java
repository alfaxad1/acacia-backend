package com.example.acacia.service;

import com.example.acacia.model.Contribution;
import com.example.acacia.model.Loan;
import com.example.acacia.model.LoanRepayment;
import com.example.acacia.model.LoanRepaymentSchedule;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.LoanRepaymentRepository;
import com.example.acacia.repository.LoanRepaymentScheduleRepository;
import com.example.acacia.repository.LoanRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;

    public byte[] generateMonthlyReport(YearMonth month) {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Monthly Contribution Report - " + month));

        PdfPTable table = new PdfPTable(3);
        table.addCell("Member");
        table.addCell("Amount");
        table.addCell("Date");

        YearMonth m = YearMonth.now();
        LocalDate start = m.atDay(1);
        LocalDate end = m.atEndOfMonth();

        List<Contribution> contributions = contributionRepository.findByMonth(start, end);

        for (Contribution c : contributions) {
            table.addCell(c.getMember().getFullName());
            table.addCell(c.getAmount().toString());
            table.addCell(c.getPaymentDate().toString());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public byte[] generateLoanStatement(Long loanId) {

        Loan loan = loanRepository.findById(loanId).orElseThrow();

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Loan Statement"));
        document.add(new Paragraph("Member: " + loan.getMember().getFullName()));
        document.add(new Paragraph("Approved Amount: " + loan.getApprovedAmount()));

        PdfPTable table = new PdfPTable(3);
        table.addCell("ID");
        table.addCell("Amount");
        table.addCell("Date");

        for (LoanRepayment lr :
                loanRepaymentRepository.findByLoan(loan)) {
            table.addCell(lr.getId().toString());
            table.addCell(lr.getAmount().toString());
            table.addCell(lr.getPaymentDate().toString());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }


}
