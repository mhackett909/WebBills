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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RecycleService {
    private static final Logger logger = LoggerFactory.getLogger(RecycleService.class);

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
        logger.info("Fetching recycle bin for user: {}", userName);
        Optional<User> user = userService.findByUsername(userName);
        if (user.isEmpty()) {
            logger.warn("User not found: {}", userName);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format(Exceptions.USER_NOT_FOUND, userName)
            );
        }

        List<Bill> recycledBills = billRepository.findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(user.get());
        logger.debug("Recycled bills for user '{}': {}", userName, recycledBills);

        List<Entry> recycledEntries;
        if (recycledBills == null || recycledBills.isEmpty()) {
            recycledEntries = entryRepository.findAllByUserAndRecycleDateIsNotNull(user.get());
        } else {
            recycledEntries = entryRepository.findAllByUserAndRecycleDateIsNotNullAndBillNotIn(user.get(), recycledBills);
        }
        logger.debug("Recycled entries for user '{}': {}", userName, recycledEntries);

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
        logger.debug("Recycled payments for user '{}': {}", userName, recycledPayments);

        RecycleDTOList dtoList = recycleMapper.buildRecycleDTOList(recycledBills, recycledEntries, recycledPayments);
        logger.info("Recycle bin built for user '{}': {}", userName, dtoList);
        return dtoList;
    }
}
