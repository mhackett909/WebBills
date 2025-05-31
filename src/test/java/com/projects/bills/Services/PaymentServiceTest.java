package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.PaymentMapper;
import com.projects.bills.Repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private PaymentRepository paymentRepository;
    private EntryService entryService;
    private PaymentMapper paymentMapper;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        entryService = mock(EntryService.class);
        paymentMapper = mock(PaymentMapper.class);
        paymentService = new PaymentService(paymentRepository, entryService, paymentMapper);
    }

    @Test
    void testGetPayments_Success() {
        Entry entry = new Entry();
        entry.setId(1L);
        entry.setBill(new Bill());
        entry.getBill().setUser(new User());
        entry.getBill().getUser().setUsername("alice");

        when(entryService.getEntryById(1L)).thenReturn(Optional.of(entry));
        List<Payment> payments = List.of(new Payment());
        when(paymentRepository.findAllByEntryIdAndRecycleDateIsNullOrderByRecycleDateDesc(1L)).thenReturn(payments);
        PaymentDTOList dtoList = new PaymentDTOList();
        when(paymentMapper.mapToDtoList(payments)).thenReturn(dtoList);

        PaymentDTOList result = paymentService.getPayments(1L, "alice");
        assertSame(dtoList, result);
    }

    @Test
    void testGetPayments_Forbidden() {
        Entry entry = new Entry();
        entry.setId(1L);
        Bill bill = new Bill();
        User user = new User();
        user.setUsername("bob");
        bill.setUser(user);
        entry.setBill(bill);

        when(entryService.getEntryById(1L)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.getPayments(1L, "alice"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void testGetPaymentById_Success() {
        Payment payment = new Payment();
        Entry entry = new Entry();
        entry.setId(2L);
        Bill bill = new Bill();
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);
        entry.setBill(bill);
        payment.setEntry(entry);

        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(entryService.getEntryById(2L)).thenReturn(Optional.of(entry));
        PaymentDTO dto = new PaymentDTO();
        when(paymentMapper.mapToPaymentDTO(payment)).thenReturn(dto);

        PaymentDTO result = paymentService.getPaymentById(10L, "alice");
        assertSame(dto, result);
    }

    @Test
    void testGetPaymentById_NotFound() {
        when(paymentRepository.findById(10L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.getPaymentById(10L, "alice"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void testCreatePayment_Success() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEntryId(3L);


        Bill bill = new Bill();
        bill.setStatus(true);
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);

        Entry entry = new Entry();
        entry.setId(3L);
        entry.setBill(bill);
        entry.setUser(user);
        entry.setAmount(BigDecimal.ONE);

        Payment payment = new Payment();
        Payment savedPayment = new Payment();
        PaymentDTO resultDTO = new PaymentDTO();

        when(entryService.getEntryById(3L)).thenReturn(Optional.of(entry));
        when(paymentMapper.buildPaymentFromDTO(any(), any(), any())).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(savedPayment);
        when(paymentMapper.mapToPaymentDTO(savedPayment)).thenReturn(resultDTO);
        when(paymentRepository.sumAmountByEntryIdAndRecycleDateIsNull(3L)).thenReturn(BigDecimal.TEN);
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setFlow("INCOMING");
        when(entryService.getEntryDtoById(3L, null, "alice")).thenReturn(Optional.of(entryDTO));
        when(entryService.saveEntry(any(), eq(true), any(), eq("alice"))).thenReturn(entryDTO);

        PaymentDTO result = paymentService.createPayment(paymentDTO, "alice");
        assertSame(resultDTO, result);
    }

    @Test
    void testCreatePayment_ArchivedBill_Forbidden() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEntryId(3L);

        Entry entry = new Entry();
        entry.setId(3L);
        Bill bill = new Bill();
        bill.setStatus(false);
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);
        entry.setBill(bill);

        when(entryService.getEntryById(3L)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.createPayment(paymentDTO, "alice"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void testUpdatePayment_Success() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEntryId(4L);
        paymentDTO.setPaymentId(20L);

        Bill bill = new Bill();
        bill.setStatus(true);
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);

        Entry entry = new Entry();
        entry.setId(4L);
        entry.setBill(bill);
        entry.setUser(user);
        entry.setAmount(BigDecimal.TEN);

        Payment payment = new Payment();
        payment.setRecycleDate(null);

        Payment savedPayment = new Payment();
        PaymentDTO resultDTO = new PaymentDTO();

        when(entryService.getEntryById(4L)).thenReturn(Optional.of(entry));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(payment));
        when(paymentMapper.buildPaymentFromDTO(paymentDTO, payment, entry)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(savedPayment);
        when(paymentMapper.mapToPaymentDTO(savedPayment)).thenReturn(resultDTO);
        when(paymentRepository.sumAmountByEntryIdAndRecycleDateIsNull(4L)).thenReturn(BigDecimal.ZERO);
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setFlow("INCOMING");
        when(entryService.getEntryDtoById(4L, null, "alice")).thenReturn(Optional.of(entryDTO));
        when(entryService.saveEntry(any(), eq(true), any(), eq("alice"))).thenReturn(entryDTO);

        PaymentDTO result = paymentService.updatePayment(paymentDTO, null, "alice");
        assertSame(resultDTO, result);
    }

    @Test
    void testUpdatePayment_RecycledPayment_Forbidden() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEntryId(4L);
        paymentDTO.setPaymentId(20L);

        Entry entry = new Entry();
        entry.setId(4L);
        Bill bill = new Bill();
        bill.setStatus(true);
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);
        entry.setBill(bill);

        Payment payment = new Payment();
        payment.setRecycleDate(java.time.LocalDateTime.now());

        when(entryService.getEntryById(4L)).thenReturn(Optional.of(entry));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(payment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.updatePayment(paymentDTO, null, "alice"));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void testUpdatePayment_NotFound() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEntryId(4L);
        paymentDTO.setPaymentId(20L);

        Entry entry = new Entry();
        entry.setId(4L);
        Bill bill = new Bill();
        bill.setStatus(true);
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);
        entry.setBill(bill);

        when(entryService.getEntryById(4L)).thenReturn(Optional.of(entry));
        when(paymentRepository.findById(20L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.updatePayment(paymentDTO, null, "alice"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void testUpdatePayment_ArchivedBill_Forbidden() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEntryId(5L);
        paymentDTO.setPaymentId(30L);

        Bill bill = new Bill();
        bill.setStatus(false); // Bill is archived
        User user = new User();
        user.setUsername("alice");
        bill.setUser(user);

        Entry entry = new Entry();
        entry.setId(5L);
        entry.setBill(bill);
        entry.setUser(user);

        when(entryService.getEntryById(5L)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.updatePayment(paymentDTO, null, "alice"));
        assertEquals(403, ex.getStatusCode().value());
        assertEquals(Exceptions.CANNOT_UPDATE_PAYMENT_ARCHIVED, ex.getReason());
    }
}

