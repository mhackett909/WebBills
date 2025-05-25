package com.projects.bills.Mappers;

import com.projects.bills.DTOs.RecycleDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

class RecycleMapperTest {
    private final RecycleMapper mapper;

    public RecycleMapperTest() {
        mapper = new RecycleMapper();
    }

    @Test
    void testBuildRecycleDTOList() {
        // Bill variables
        Long billId = 100L;
        String billName = "Acme Corp";
        Boolean billStatus = false;
        LocalDateTime billRecycleDate = LocalDateTime.now().minusDays(1);

        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setName(billName);
        bill.setStatus(billStatus);
        bill.setRecycleDate(billRecycleDate);

        // Entry variables
        Long entryId = 200L;
        Date entryDate = Date.valueOf("2023-10-01");
        LocalDateTime entryRecycleDate = LocalDateTime.now().minusHours(5);
        BigDecimal entryAmount = new BigDecimal("500.00");
        String entryFlow = "INCOMING";
        String entryServices = "Consulting";

        Entry entry = new Entry();
        entry.setId(entryId);
        entry.setDate(entryDate);
        entry.setRecycleDate(entryRecycleDate);
        entry.setAmount(entryAmount);
        entry.setFlow(entryFlow);
        entry.setServices(entryServices);
        entry.setBill(bill);

        // Payment variables
        Long paymentId = 300L;
        Date paymentDate = Date.valueOf("2023-10-01");
        LocalDateTime paymentRecycleDate = LocalDateTime.now();
        BigDecimal paymentAmount = new BigDecimal("250.00");
        String paymentType = "Credit";
        String paymentMedium = "Bank";
        String paymentNotes = "Partial payment";

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setDate(paymentDate);
        payment.setRecycleDate(paymentRecycleDate);
        payment.setAmount(paymentAmount);
        payment.setType(paymentType);
        payment.setMedium(paymentMedium);
        payment.setNotes(paymentNotes);

        Entry paymentEntry = new Entry();
        paymentEntry.setBill(bill);
        payment.setEntry(paymentEntry);

        // Lists
        List<Bill> bills = List.of(bill);
        List<Entry> entries = List.of(entry);
        List<Payment> payments = List.of(payment);

        // Act
        RecycleDTOList dtoList = mapper.buildRecycleDTOList(bills, entries, payments);

        // Assert
        assertNotNull(dtoList);
        assertEquals(3, dtoList.getRecycleItems().size());

        // Payment (should be first, most recent recycleDate)
        assertEquals(paymentId, dtoList.getRecycleItems().get(0).getEntityId());
        assertEquals("Payment", dtoList.getRecycleItems().get(0).getEntityType());
        assertEquals(paymentDate, dtoList.getRecycleItems().get(0).getEntityDate());
        assertEquals(paymentRecycleDate, dtoList.getRecycleItems().get(0).getRecycleDate());
        assertEquals(billName, dtoList.getRecycleItems().get(0).getPartyName());
        assertEquals(billId, dtoList.getRecycleItems().get(0).getInvoiceNumber());
        assertEquals(paymentAmount, dtoList.getRecycleItems().get(0).getAmount());
        assertEquals(paymentType + " / " + paymentMedium, dtoList.getRecycleItems().get(0).getType());
        assertEquals(paymentNotes, dtoList.getRecycleItems().get(0).getDetails());

        // Entry (should be second)
        assertEquals(entryId, dtoList.getRecycleItems().get(1).getEntityId());
        assertEquals("Entry", dtoList.getRecycleItems().get(1).getEntityType());
        assertEquals(entryDate, dtoList.getRecycleItems().get(1).getEntityDate());
        assertEquals(entryRecycleDate, dtoList.getRecycleItems().get(1).getRecycleDate());
        assertEquals(billName, dtoList.getRecycleItems().get(1).getPartyName());
        assertEquals(entryId, dtoList.getRecycleItems().get(1).getInvoiceNumber());
        assertEquals(entryAmount, dtoList.getRecycleItems().get(1).getAmount());
        assertEquals("Income", dtoList.getRecycleItems().get(1).getType());
        assertEquals(entryServices, dtoList.getRecycleItems().get(1).getDetails());

        // Bill (should be last, oldest recycleDate)
        assertEquals(billId, dtoList.getRecycleItems().get(2).getEntityId());
        assertEquals("Party", dtoList.getRecycleItems().get(2).getEntityType());
        assertNull(dtoList.getRecycleItems().get(2).getEntityDate());
        assertEquals(billRecycleDate, dtoList.getRecycleItems().get(2).getRecycleDate());
        assertEquals(billName, dtoList.getRecycleItems().get(2).getPartyName());
        assertNull(dtoList.getRecycleItems().get(2).getInvoiceNumber());
        assertNull(dtoList.getRecycleItems().get(2).getAmount());
        assertEquals("Archived", dtoList.getRecycleItems().get(2).getType());
        assertNull(dtoList.getRecycleItems().get(2).getDetails());
    }
}