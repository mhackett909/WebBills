package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.RecycleDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.RecycleMapper;
import com.projects.bills.Repositories.BillRepository;
import com.projects.bills.Repositories.EntryRepository;
import com.projects.bills.Repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecycleServiceTest {

    private BillRepository billRepository;
    private EntryRepository entryRepository;
    private PaymentRepository paymentRepository;
    private UserService userService;
    private RecycleMapper recycleMapper;
    private RecycleService recycleService;

    @BeforeEach
    void setUp() {
        billRepository = mock(BillRepository.class);
        entryRepository = mock(EntryRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        userService = mock(UserService.class);
        recycleMapper = mock(RecycleMapper.class);
        recycleService = new RecycleService(billRepository, entryRepository, paymentRepository, userService, recycleMapper);
    }

    @Test
    void testGetRecycleBin_UserNotFound_Throws() {
        when(userService.findByUsername("alice")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                recycleService.getRecycleBin("alice"));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(String.format(Exceptions.USER_NOT_FOUND, "alice"), ex.getReason());
    }

    @Test
    void testGetRecycleBin_EmptyRecycleBin() {
        User user = new User();
        when(userService.findByUsername("bob")).thenReturn(Optional.of(user));
        when(billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user)).thenReturn(Collections.emptyList());
        when(entryRepository.findAllByUserAndRecycleDateIsNotNull(user)).thenReturn(Collections.emptyList());
        when(paymentRepository.findAllByUserAndRecycleDateIsNotNull(user)).thenReturn(Collections.emptyList());

        RecycleDTOList expected = new RecycleDTOList();
        when(recycleMapper.buildRecycleDTOList(anyList(), anyList(), anyList())).thenReturn(expected);

        RecycleDTOList result = recycleService.getRecycleBin("bob");
        assertSame(expected, result);
    }

    @Test
    void testGetRecycleBin_WithRecycledBillsEntriesPayments() {
        User user = new User();
        List<Bill> bills = List.of(new Bill());
        List<Entry> entries = List.of(new Entry());
        List<Payment> payments = List.of(new Payment());

        when(userService.findByUsername("carol")).thenReturn(Optional.of(user));
        when(billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user)).thenReturn(bills);
        when(entryRepository.findAllByUserAndRecycleDateIsNotNullAndBillNotIn(user, bills)).thenReturn(entries);
        when(paymentRepository.findAllByUserAndRecycleDateIsNotNullAndEntryNotInAndBillNotIn(user, entries, bills)).thenReturn(payments);

        RecycleDTOList expected = new RecycleDTOList();
        when(recycleMapper.buildRecycleDTOList(bills, entries, payments)).thenReturn(expected);

        RecycleDTOList result = recycleService.getRecycleBin("carol");
        assertSame(expected, result);
    }

    @Test
    void testGetRecycleBin_RecycledBillsNotEmpty_RecycledEntriesEmpty() {
        User user = new User();
        List<Bill> bills = List.of(new Bill());
        List<Entry> entries = Collections.emptyList();
        List<Payment> payments = List.of(new Payment());

        when(userService.findByUsername("dave")).thenReturn(Optional.of(user));
        when(billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user)).thenReturn(bills);
        when(entryRepository.findAllByUserAndRecycleDateIsNotNullAndBillNotIn(user, bills)).thenReturn(entries);
        when(paymentRepository.findAllByUserAndRecycleDateIsNotNullAndBillNotIn(user, bills)).thenReturn(payments);

        RecycleDTOList expected = new RecycleDTOList();
        when(recycleMapper.buildRecycleDTOList(bills, entries, payments)).thenReturn(expected);

        RecycleDTOList result = recycleService.getRecycleBin("dave");
        assertSame(expected, result);
    }

    @Test
    void testGetRecycleBin_RecycledBillsEmpty_RecycledEntriesNotEmpty() {
        User user = new User();
        List<Bill> bills = Collections.emptyList();
        List<Entry> entries = List.of(new Entry());
        List<Payment> payments = List.of(new Payment());

        when(userService.findByUsername("eve")).thenReturn(Optional.of(user));
        when(billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user)).thenReturn(bills);
        when(entryRepository.findAllByUserAndRecycleDateIsNotNull(user)).thenReturn(entries);
        when(paymentRepository.findAllByUserAndRecycleDateIsNotNullAndEntryNotIn(user, entries)).thenReturn(payments);

        RecycleDTOList expected = new RecycleDTOList();
        when(recycleMapper.buildRecycleDTOList(bills, entries, payments)).thenReturn(expected);

        RecycleDTOList result = recycleService.getRecycleBin("eve");
        assertSame(expected, result);
    }

    @Test
    void testGetRecycleBin_RecycledBillsAndEntriesEmpty() {
        User user = new User();
        List<Bill> bills = Collections.emptyList();
        List<Entry> entries = Collections.emptyList();
        List<Payment> payments = List.of(new Payment());

        when(userService.findByUsername("frank")).thenReturn(Optional.of(user));
        when(billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user)).thenReturn(bills);
        when(entryRepository.findAllByUserAndRecycleDateIsNotNull(user)).thenReturn(entries);
        when(paymentRepository.findAllByUserAndRecycleDateIsNotNull(user)).thenReturn(payments);

        RecycleDTOList expected = new RecycleDTOList();
        when(recycleMapper.buildRecycleDTOList(bills, entries, payments)).thenReturn(expected);

        RecycleDTOList result = recycleService.getRecycleBin("frank");
        assertSame(expected, result);
    }
}

