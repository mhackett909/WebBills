package com.projects.bills.Services;
import com.projects.bills.Constants.Strings;
import com.projects.bills.DTOs.BillDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.BillMapper;
import com.projects.bills.Repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {
	private final BillRepository billRepository;
	private final UserService userService;
	private final BillMapper billMapper;

	@Autowired
	public BillService(BillRepository billRepository, UserService userService, BillMapper billMapper) {
		this.billRepository = billRepository;
        this.userService = userService;
        this.billMapper = billMapper;
    }

	protected List<Bill> getBills(String userName) {
		Optional<User> realUser = userService.findByUsername(userName);
		if (realUser.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		return billRepository.findAllByUserAndRecycleDateIsNullOrderByNameAsc(realUser.get());
	}

	protected Bill getBillEntityById(Long id) {
		return billRepository.findById(id).orElse(null);
	}

	public BillDTOList getBillDtoList(String filter, String userName) {
		Optional<User> user = userService.findByUsername(userName);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}

		List<Bill> bills;

		if ("active".equalsIgnoreCase(filter)) {
			bills = billRepository.findAllByStatusAndUserAndRecycleDateIsNullOrderByNameAsc(true, user.get());
		} else if ("inactive".equalsIgnoreCase(filter)) {
			bills = billRepository.findAllByStatusAndUserAndRecycleDateIsNullOrderByNameAsc(false, user.get());
		} else {
			bills = billRepository.findAllByUserAndRecycleDateIsNullOrderByNameAsc(user.get());
		}

		return billMapper.mapToDTOList(bills);
	}

	public BillDTO getBill(Long id, String filter, String userName) {
		Bill bill = billRepository.findById(id).orElse(null);
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill does not exist by id: " + id);
		}

		if (!bill.getUser().getUsername().equalsIgnoreCase(userName)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to access this bill");
		}

		// "bypass" filter allows access to recycled bills for restoration
		if (bill.getRecycleDate() != null && !Strings.EDIT_BYPASS.equalsIgnoreCase(filter)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bill is recycled");
		}

		return billMapper.mapToDTO(bill);
	}

	public BillDTO saveBill(BillDTO billTransfer, boolean existing, String userName) {
		Optional<User> user = userService.findByUsername(userName);
		if (user.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		Bill bill;
		if (existing) {
			// Load existing bill by ID
			bill = billRepository.findById(billTransfer.getId()).orElse(null);
			if (bill == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found with id: " + billTransfer.getId());
			}
			if (!bill.getUser().getUsername().equalsIgnoreCase(userName)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to access this bill");
			}
		} else {
			bill = new Bill();
		}

		bill.setName(billTransfer.getName());
		bill.setStatus(billTransfer.getStatus());
		bill.setUser(user.get());

		if (billTransfer.getRecycle()) {
			bill.setRecycleDate(LocalDateTime.now());
		} else {
			bill.setRecycleDate(null);
		}

		Bill updatedBill = billRepository.save(bill);
		return billMapper.mapToDTO(updatedBill);
	}
}
