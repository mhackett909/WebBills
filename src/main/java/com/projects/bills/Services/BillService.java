package com.projects.bills.Services;
import com.projects.bills.Constants.Strings;
import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.BillDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.BillMapper;
import com.projects.bills.Repositories.BillRepository;
import com.projects.bills.Specifications.BillSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {
	private static final Logger logger = LoggerFactory.getLogger(BillService.class);

	private final BillRepository billRepository;
	private final UserService userService;
	private final BillMapper billMapper;

	@Autowired
	public BillService(BillRepository billRepository, UserService userService, BillMapper billMapper) {
		this.billRepository = billRepository;
        this.userService = userService;
        this.billMapper = billMapper;
    }

	public BillDTOList getBillDtoList(String status, String userName) {
		Optional<User> user = userService.findByUsername(userName);
		if (user.isEmpty()) {
			logger.error("User not found: {}", userName);
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					String.format(Exceptions.USER_NOT_FOUND, userName)
			);
		}

		logger.info("Fetching bills for user: {} with status: {}", userName, status);

		Boolean statusBool = null;
		if ("active".equalsIgnoreCase(status)) {
			statusBool = true;
		} else if ("inactive".equalsIgnoreCase(status)) {
			statusBool = false;
		}

		Specification<Bill> spec = BillSpecification.filterBills(statusBool, user.get());
		List<Bill> bills = billRepository.findAll(spec);

		logger.info("Found {} bills for user: {}", bills.size(), userName);
		return billMapper.mapToDTOList(bills);
	}

	public BillDTO getBill(Long id, String bypass, String userName) {
		Bill bill = billRepository.findById(id).orElse(null);
		if (bill == null) {
			logger.error("Bill not found with ID: {}", id);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(Exceptions.BILL_NOT_FOUND, id));
		}

		if (!bill.getUser().getUsername().equalsIgnoreCase(userName)) {
			logger.error("User {} is not authorized to access bill with ID: {}", userName, id);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_BILL);
		}

		// "bypass" filter allows access to recycled bills for restoration
		if (bill.getRecycleDate() != null && !Strings.EDIT_BYPASS.equalsIgnoreCase(bypass)) {
			logger.error("Bill with ID: {} is recycled and cannot be accessed", id);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.BILL_IS_RECYCLED);
		}

		logger.info("Returning bill with ID: {} for user: {}", id, userName);
		return billMapper.mapToDTO(bill);
	}

	public BillDTO saveBill(BillDTO billTransfer, boolean existing, String userName) {
		Optional<User> user = userService.findByUsername(userName);
		if (user.isEmpty()) {
			logger.error("User not found: {}", userName);
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					String.format(Exceptions.USER_NOT_FOUND, userName)
			);
		}

		Bill bill;
		if (existing) {
			// Load existing bill by ID
			bill = billRepository.findById(billTransfer.getId()).orElse(null);
			if (bill == null) {
				logger.error("Bill not found with ID: {}", billTransfer.getId());
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(Exceptions.BILL_NOT_FOUND, billTransfer.getId()));
			}
			if (!bill.getUser().getUsername().equalsIgnoreCase(userName)) {
				logger.error("User {} is not authorized to update bill with ID: {}", userName, billTransfer.getId());
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_BILL);
			}
		} else {
			bill = new Bill();
		}

		bill.setName(billTransfer.getName());
		bill.setStatus(billTransfer.getStatus());
		bill.setUser(user.get());
		bill.setInternal(billTransfer.getInternal());
		if (billTransfer.getCategory() != null) {
			bill.setCategory(billTransfer.getCategory().toLowerCase());
		} else {
			bill.setCategory("uncategorized");
		}

		if (billTransfer.getRecycle()) {
			bill.setRecycleDate(LocalDateTime.now());
		} else {
			bill.setRecycleDate(null);
		}

		logger.debug("Saving bill: {}", bill);
		Bill updatedBill = billRepository.save(bill);
		logger.info("Bill saved with ID {} and name {}", updatedBill.getBillId(), updatedBill.getName());
		return billMapper.mapToDTO(updatedBill);
	}

	protected Bill getBillEntityById(Long id) {
		logger.info("Fetching bill by ID: {}", id);
		return billRepository.findById(id).orElse(null);
	}
}
