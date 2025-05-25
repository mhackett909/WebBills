package com.projects.bills.Mappers;

import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    private final PaymentMapper mapper;

    public PaymentMapperTest() {
        mapper = new PaymentMapper();
    }

    @Test
    void testMapToPaymentDTO() {
        long paymentId = 10L;
        long entryId = 5L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        BigDecimal amount = new BigDecimal("123.45");
        String type = "Credit";
        String medium = "Bank";
        String notes = "Test payment";
        LocalDateTime recycleDate = LocalDateTime.now();

        Entry entry = new Entry();
        entry.setId(entryId);

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setDate(Date.valueOf(date));
        payment.setAmount(amount);
        payment.setType(type);
        payment.setMedium(medium);
        payment.setNotes(notes);
        payment.setRecycleDate(recycleDate);
        payment.setEntry(entry);

        PaymentDTO dto = mapper.mapToPaymentDTO(payment);

        assertNotNull(dto);
        assertEquals(paymentId, dto.getPaymentId());
        assertEquals(entryId, dto.getEntryId());
        assertEquals(date, dto.getDate());
        assertEquals(amount, dto.getAmount());
        assertEquals(type, dto.getType());
        assertEquals(medium, dto.getMedium());
        assertEquals(notes, dto.getNotes());
        assertTrue(dto.getRecycle());
    }

    @Test
    void testBuildPaymentFromDTO() {
        long entryId = 7L;
        LocalDate date = LocalDate.of(2024, 5, 20);
        BigDecimal amount = new BigDecimal("50.00");
        String type = "Debit";
        String medium = "Cash";
        String notes = "Another payment";
        boolean recycle = true;

        PaymentDTO dto = new PaymentDTO();
        dto.setDate(date);
        dto.setAmount(amount);
        dto.setType(type);
        dto.setMedium(medium);
        dto.setNotes(notes);
        dto.setRecycle(recycle);

        Entry entry = new Entry();
        entry.setId(entryId);

        Payment payment = new Payment();

        Payment result = mapper.buildPaymentFromDTO(dto, payment, entry);

        assertNotNull(result);
        assertEquals(Date.valueOf(date), result.getDate());
        assertEquals(amount, result.getAmount());
        assertEquals(type, result.getType());
        assertEquals(medium, result.getMedium());
        assertEquals(notes, result.getNotes());
        assertEquals(entry, result.getEntry());
        assertNotNull(result.getRecycleDate());
    }

    @Test
    void testMapToDtoList() {
        long paymentId1 = 1L;
        long paymentId2 = 2L;
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 2, 2);
        BigDecimal amount1 = new BigDecimal("10.00");
        BigDecimal amount2 = new BigDecimal("20.00");
        String type1 = "Credit";
        String type2 = "Debit";
        String medium1 = "Bank";
        String medium2 = "Cash";
        String notes1 = "First";
        String notes2 = "Second";
        LocalDateTime recycleDate2 = LocalDateTime.now();

        Payment payment1 = new Payment();
        payment1.setPaymentId(paymentId1);
        payment1.setDate(Date.valueOf(date1));
        payment1.setAmount(amount1);
        payment1.setType(type1);
        payment1.setMedium(medium1);
        payment1.setNotes(notes1);
        payment1.setRecycleDate(null);

        Payment payment2 = new Payment();
        payment2.setPaymentId(paymentId2);
        payment2.setDate(Date.valueOf(date2));
        payment2.setAmount(amount2);
        payment2.setType(type2);
        payment2.setMedium(medium2);
        payment2.setNotes(notes2);
        payment2.setRecycleDate(recycleDate2);

        List<Payment> payments = Arrays.asList(payment1, payment2);

        PaymentDTOList dtoList = mapper.mapToDtoList(payments);

        assertNotNull(dtoList);
        assertEquals(2, dtoList.getPaymentDTOList().size());
        assertEquals(paymentId1, dtoList.getPaymentDTOList().get(0).getPaymentId());
        assertEquals(paymentId2, dtoList.getPaymentDTOList().get(1).getPaymentId());
        assertFalse(dtoList.getPaymentDTOList().get(0).getRecycle());
        assertTrue(dtoList.getPaymentDTOList().get(1).getRecycle());
    }
}