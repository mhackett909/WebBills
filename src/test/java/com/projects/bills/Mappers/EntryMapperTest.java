package com.projects.bills.Mappers;

import com.projects.bills.DTOs.BalanceDTO;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntryMapperTest {

    private final EntryMapper mapper = new EntryMapper();

    @Test
    void testMapToDTO() {
        Long entryId = 1L;
        Long billId = 2L;
        Long invoiceId = 3L;
        Long paymentId = 4L;
        String billName = "Test Bill";
        LocalDate date = LocalDate.of(2024, 6, 1);
        BigDecimal amount = new BigDecimal("123.45");
        BigDecimal paymentAmount = new BigDecimal("50.00");
        BigDecimal balance = amount.subtract(paymentAmount);
        Boolean status = true;
        Boolean recycle = true;
        String services = "Consulting";
        String flow = FlowType.OUTGOING.toString();
        Boolean archived = false;
        Boolean overpaid = true;
        LocalDateTime recycleDate = LocalDateTime.now();

        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setName(billName);

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setAmount(paymentAmount);

        Entry entry = new Entry();
        entry.setId(entryId);
        entry.setBill(bill);
        entry.setInvoiceId(invoiceId);
        entry.setDate(Date.valueOf(date));
        entry.setAmount(amount);
        entry.setStatus(status);
        entry.setRecycleDate(recycleDate);
        entry.setServices(services);
        entry.setFlow(flow);
        entry.setOverpaid(overpaid);
        entry.setPayments(List.of(payment));
        entry.setBalance(balance); // Set balance explicitly for test

        EntryDTO dto = mapper.mapToDTO(entry, archived);

        assertNotNull(dto);
        assertEquals(entryId, dto.getEntryId());
        assertEquals(billId, dto.getBillId());
        assertEquals(invoiceId, dto.getInvoiceId());
        assertEquals(billName, dto.getName());
        assertEquals(date, dto.getDate());
        assertEquals(amount, dto.getAmount());
        assertEquals(status, dto.getStatus());
        assertEquals(recycle, dto.getRecycle());
        assertEquals(services, dto.getServices());
        assertEquals(flow, dto.getFlow());
        assertEquals(archived, dto.getArchived());
        assertEquals(overpaid, dto.getOverpaid());
        assertEquals(balance, dto.getBalance().getTotalBalance());
        assertEquals(BigDecimal.ZERO, dto.getBalance().getTotalOverpaid());
    }

    @Test
    void testMapToDTO_NegativeBalance_SetsTotalOverpaid() {
        Long entryId = 1L;
        Long billId = 2L;
        Long invoiceId = 3L;
        Long paymentId = 4L;
        String billName = "Test Bill";
        LocalDate date = LocalDate.of(2024, 6, 1);
        BigDecimal amount = new BigDecimal("123.45");
        BigDecimal paymentAmount = new BigDecimal("150.00");
        BigDecimal overPaid = amount.subtract(paymentAmount).abs();
        Boolean status = true;
        String flow = FlowType.OUTGOING.toString();
        Boolean overpaid = true;


        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setName(billName);

        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setAmount(paymentAmount);

        Entry entry = new Entry();
        entry.setId(entryId);
        entry.setBill(bill);
        entry.setInvoiceId(invoiceId);
        entry.setDate(Date.valueOf(date));
        entry.setAmount(amount);
        entry.setStatus(status);
        entry.setFlow(flow);
        entry.setOverpaid(overpaid);
        entry.setBalance(overPaid.negate()); // Set balance explicitly for test
        entry.setPayments(List.of(payment));

        EntryDTO dto = mapper.mapToDTO(entry, true);

        assertNotNull(dto);
        assertEquals(entryId, dto.getEntryId());
        assertEquals(billId, dto.getBillId());
        assertEquals(invoiceId, dto.getInvoiceId());
        assertEquals(billName, dto.getName());
        assertEquals(date, dto.getDate());
        assertEquals(amount, dto.getAmount());
        assertEquals(status, dto.getStatus());
        assertEquals(flow, dto.getFlow());
        assertEquals(overpaid, dto.getOverpaid());
        assertEquals(overPaid, dto.getBalance().getTotalOverpaid());
        assertEquals(BigDecimal.ZERO, dto.getBalance().getTotalBalance());
    }

    @Test
    void testMapToEntity() {
        Long entryId = 10L;
        Long billId = 20L;
        Long invoiceId = 30L;
        String billName = "Acme";
        LocalDate date = LocalDate.of(2024, 5, 15);
        BigDecimal amount = new BigDecimal("555.55");
        Boolean status = false;
        String services = "Design";
        String flow = FlowType.INCOMING.toString();
        Boolean recycle = true;

        EntryDTO dto = new EntryDTO();
        dto.setEntryId(entryId);
        dto.setBillId(billId);
        dto.setInvoiceId(invoiceId);
        dto.setName(billName);
        dto.setDate(date);
        dto.setAmount(amount);
        dto.setStatus(status);
        dto.setServices(services);
        dto.setFlow(flow);
        dto.setRecycle(recycle);

        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setName(billName);

        User user = new User();
        user.setId(99L);

        Entry entry = new Entry();

        Entry result = mapper.mapToEntity(dto, entry, bill, FlowType.INCOMING, user, invoiceId);

        assertNotNull(result);
        assertEquals(entryId, result.getId());
        assertEquals(bill, result.getBill());
        assertEquals(user, result.getUser());
        assertEquals(Date.valueOf(date), result.getDate());
        assertEquals(amount, result.getAmount());
        assertEquals(status, result.getStatus());
        assertEquals(services, result.getServices());
        assertEquals(flow, result.getFlow());
        assertEquals(false, result.getOverpaid());
        assertEquals(invoiceId, result.getInvoiceId());
        assertNotNull(result.getRecycleDate());
    }

    @Test
    void testMapEntriesToDTOList() {
        Long entryId1 = 1L;
        Long entryId2 = 2L;
        Long billId = 5L;
        Long invoiceId1 = 11L;
        Long invoiceId2 = 12L;
        String name1 = "Bill1";
        String name2 = "Bill2";
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 2, 2);
        BigDecimal amount1 = new BigDecimal("10.00");
        BigDecimal amount2 = new BigDecimal("20.00");
        Boolean status1 = true;
        Boolean status2 = false;
        Boolean recycle1 = false;
        Boolean recycle2 = true;
        String services1 = "A";
        String services2 = "B";
        String flow1 = FlowType.OUTGOING.toString();
        String flow2 = FlowType.INCOMING.toString();
        Boolean archived1 = false;
        Boolean archived2 = true;
        Boolean overpaid1 = false;
        Boolean overpaid2 = true;

        EntryDTO dto1 = new EntryDTO(entryId1, billId, invoiceId1, name1, date1, amount1,
                new BalanceDTO(), status1, recycle1, services1, flow1, archived1, overpaid1);
        EntryDTO dto2 = new EntryDTO(entryId2, billId, invoiceId2, name2, date2, amount2,
                new BalanceDTO(), status2, recycle2, services2, flow2, archived2, overpaid2);

        ArrayList<EntryDTO> list = new ArrayList<>(Arrays.asList(dto1, dto2));
        Long total = 2L;

        EntryDTOList dtoList = mapper.mapEntriesToDTOList(list, total);

        assertNotNull(dtoList);
        assertEquals(total, dtoList.getTotal());
        assertEquals(2, dtoList.getEntries().size());
        assertEquals(entryId1, dtoList.getEntries().get(0).getEntryId());
        assertEquals(entryId2, dtoList.getEntries().get(1).getEntryId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"overpaid", "true", "false"})
    void testMapToEntryFilters(String paid) {
        String userName = "user";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long invoiceNum = 123L;
        List<String> partyList = Arrays.asList("A", "B");
        BigDecimal min = new BigDecimal("10.00");
        BigDecimal max = new BigDecimal("100.00");
        String flow = "Income";
        String archives = "true";

        EntryFilters filters = mapper.mapToEntryFilters(userName, startDate, endDate, invoiceNum, partyList, min, max, flow, paid, archives);

        assertNotNull(filters);
        assertEquals(userName, filters.getUserName());
        assertEquals(startDate, filters.getStartDate());
        assertEquals(endDate, filters.getEndDate());
        assertEquals(invoiceNum, filters.getInvoiceNum());
        assertEquals(partyList, filters.getPartyList());
        assertEquals(min, filters.getMin());
        assertEquals(max, filters.getMax());
        assertEquals(FlowType.INCOMING.toString(), filters.getFlow());
        if (paid.equals("overpaid")) {
            assertNull(filters.getPaid());
            assertTrue(filters.getOverpaid());
        } else if (paid.equals("true")) {
            assertTrue(filters.getPaid());
            assertNull(filters.getOverpaid());
        } else if (paid.equals("false")) {
            assertFalse(filters.getPaid());
            assertNull(filters.getOverpaid());
        }
        assertTrue(filters.getArchived());
    }

    @Test
    void testMapSortField() {
        assertEquals("status", mapper.mapSortField("paid"));
        assertEquals("bill.name", mapper.mapSortField("name"));
        assertEquals("id", mapper.mapSortField("entryId"));
        assertEquals("services", mapper.mapSortField("description"));
        assertEquals("bill.status", mapper.mapSortField("archived"));
        assertEquals("custom", mapper.mapSortField("custom"));
    }
}

