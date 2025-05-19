package com.projects.bills.Services;

import com.projects.bills.DTOs.RecycleDTO;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.EntityType;
import com.projects.bills.Repositories.BillRepository;
import com.projects.bills.Repositories.EntryRepository;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecycleService {
    private final BillRepository billRepository;
    private final EntryRepository entryRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public RecycleService(BillRepository billRepository, EntryRepository entryRepository, PaymentRepository paymentRepository, UserService userService) {
        this.billRepository = billRepository;
        this.entryRepository = entryRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    public List<RecycleDTO> getRecycleBin() {
        String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(requestingUser);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        List<Bill> recycledBills = billRepository.findAllByUserAndRecycleDateIsNotNull(user.get());

        List<Entry> recycledEntries;
        if (recycledBills == null || recycledBills.isEmpty()) {
            recycledEntries = entryRepository.findAllByUserAndRecycleDateIsNotNull(user.get());
        } else {
            recycledEntries = entryRepository.findAllByUserAndRecycleDateIsNotNullAndBillNotIn(user.get(), recycledBills);
        }

        List<Payment> recycledPayments;
        if (recycledEntries == null || recycledEntries.isEmpty()) {
            if (recycledBills == null || recycledBills.isEmpty()) {
                recycledPayments = paymentRepository.findAllByUserAndRecycleDateIsNotNull(user.get());
            } else {
                recycledPayments = paymentRepository.findAllByUserAndRecycleDateIsNotNullAndBillNotIn(user.get(), recycledBills);
            }
        } else {
            if (recycledBills == null || recycledBills.isEmpty()) {
                recycledPayments = paymentRepository.findAllByUserAndRecycleDateIsNotNullAndEntryNotIn(user.get(), recycledEntries);
            } else {
                recycledPayments = paymentRepository.findAllByUserAndRecycleDateIsNotNullAndEntryNotInAndBillNotIn(user.get(), recycledEntries, recycledBills);
            }
        }

        return recycleDTOList(recycledBills, recycledEntries, recycledPayments);
    }

    private List<RecycleDTO> recycleDTOList(List<Bill> recycledBills, List<Entry> recycledEntries, List<Payment> recycledPayments) {
        List<RecycleDTO> combined = new ArrayList<>();
        if (recycledBills != null) {
            recycledBills.forEach(bill -> combined.add(mapBillToRecycleDTO(bill)));
        }
        if (recycledEntries != null) {
            recycledEntries.forEach(entry -> combined.add(mapEntryToRecycleDTO(entry)));
        }
        if (recycledPayments != null) {
            recycledPayments.forEach(payment -> combined.add(mapPaymentToRecycleDTO(payment)));
        }
        combined.sort((a, b) -> b.getRecycleDate().compareTo(a.getRecycleDate())); // Descending
        return combined;
    }

    private RecycleDTO mapBillToRecycleDTO(Bill bill) {
        RecycleDTO recycleDTO = new RecycleDTO();
        recycleDTO.setEntityId(bill.getBillId());
        recycleDTO.setEntityType(EntityType.PARTY.getValue());
        recycleDTO.setRecycleDate(bill.getRecycleDate());
        recycleDTO.setPartyName(bill.getName());
        recycleDTO.setInvoiceNumber(bill.getBillId());
        recycleDTO.setType(bill.getStatus() ? "Active" : "Archived");
        return recycleDTO;
    }

    private RecycleDTO mapEntryToRecycleDTO(Entry entry) {
        Bill entryBill = entry.getBill();

        RecycleDTO recycleDTO = new RecycleDTO();
        recycleDTO.setEntityId(entry.getId());
        recycleDTO.setEntityType(EntityType.ENTRY.getValue());
        recycleDTO.setEntityDate(entry.getDate());
        recycleDTO.setRecycleDate(entry.getRecycleDate());
        recycleDTO.setPartyName(entryBill.getName());
        recycleDTO.setInvoiceNumber(entryBill.getBillId());
        recycleDTO.setAmount(entry.getAmount());
        recycleDTO.setType(entry.getFlow());
        recycleDTO.setDetails(entry.getServices());

        return recycleDTO;
    }

    private RecycleDTO mapPaymentToRecycleDTO(Payment payment) {
        Bill entryBill = payment.getEntry().getBill();

        RecycleDTO recycleDTO = new RecycleDTO();
        recycleDTO.setEntityId(payment.getPaymentId());
        recycleDTO.setEntityType(EntityType.PAYMENT.getValue());
        recycleDTO.setEntityDate(payment.getDate());
        recycleDTO.setRecycleDate(payment.getRecycleDate());
        recycleDTO.setPartyName(entryBill.getName());
        recycleDTO.setInvoiceNumber(entryBill.getBillId());
        recycleDTO.setAmount(payment.getAmount());
        recycleDTO.setType(payment.getType()+" / "+payment.getMedium());
        recycleDTO.setDetails(payment.getNotes());

        return recycleDTO;
    }
}
