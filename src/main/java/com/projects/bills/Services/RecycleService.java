package com.projects.bills.Services;

import com.projects.bills.DTOs.RecycleDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.RecycleMapper;
import com.projects.bills.Repositories.BillRepository;
import com.projects.bills.Repositories.EntryRepository;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RecycleService {
    private final BillRepository billRepository;
    private final EntryRepository entryRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final RecycleMapper recycleMapper;

    public RecycleService(BillRepository billRepository, EntryRepository entryRepository, PaymentRepository paymentRepository, UserService userService, RecycleMapper recycleMapper) {
        this.billRepository = billRepository;
        this.entryRepository = entryRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.recycleMapper = recycleMapper;
    }

    public RecycleDTOList getRecycleBin(String userName) {
        Optional<User> user = userService.findByUsername(userName);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        List<Bill> recycledBills = billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user.get());

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

        return recycleMapper.buildRecycleDTOList(recycledBills, recycledEntries, recycledPayments);
    }
}
