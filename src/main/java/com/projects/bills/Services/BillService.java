package com.projects.bills.Services;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {
	private final BillRepository billRepository;
	private final UserService userService;

	@Autowired
	public BillService(BillRepository billRepository, UserService userService) {
		this.billRepository = billRepository;
        this.userService = userService;
    }

	protected List<Bill> getBills(String userName) {
		Optional<User> realUser = userService.findByUsername(userName);
		if (realUser.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		return billRepository.findAllByUserAndRecycleDateIsNull(realUser.get());
	}

	protected Bill getBillEntityById(Long id) {
		return billRepository.findById(id).orElse(null);
	}

	public List<BillDTO> getBillDtoList(String filter, String userName) {
		Optional<User> user = userService.findByUsername(userName);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}

		String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!user.get().getUsername().equalsIgnoreCase(requestingUser)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to access these bills");
		}

		List<BillDTO> billDtoList = new ArrayList<>();
		List<Bill> bills;

		if ("active".equalsIgnoreCase(filter)) {
			bills = billRepository.findAllByStatusAndUserAndRecycleDateIsNull(true, user.get());
		} else if ("inactive".equalsIgnoreCase(filter)) {
			bills = billRepository.findAllByStatusAndUserAndRecycleDateIsNull(false, user.get());
		} else {
			bills = billRepository.findAllByUserAndRecycleDateIsNull(user.get());
		}

		for (Bill bill : bills) {
			BillDTO billDTO = mapToDTO(bill);
			billDtoList.add(billDTO);
		}

		return billDtoList;
	}

	public BillDTO getBill(Long id) {
		Bill bill = billRepository.findById(id).orElse(null);
		if (bill == null) return null;

		String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!bill.getUser().getUsername().equalsIgnoreCase(requestingUser)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to access this bill");
		}

		if (bill.getRecycleDate() != null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bill is recycled");
		}

		return mapToDTO(bill);
	}

	public BillDTO saveBill(BillDTO billTransfer, boolean existing) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userService.findByUsername(username);
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
		} else {
			bill = new Bill();
		}

		bill.setName(billTransfer.getName());
		bill.setStatus(billTransfer.getStatus());
		bill.setUser(user.get());

		if (billTransfer.getRecycle()) {
			bill.setRecycleDate(LocalDateTime.now());
			// TODO mark all entries and payments for this bill as recycled
		}

		Bill updatedBill = billRepository.save(bill);
		return mapToDTO(updatedBill);
	}

	public static BillDTO mapToDTO(Bill bill) {
		if (bill == null) return null;
		BillDTO billDTO = new BillDTO();
		billDTO.setId(bill.getBillId());
		billDTO.setName(bill.getName());
		billDTO.setStatus(bill.getStatus());
		billDTO.setRecycle(bill.getRecycleDate() != null);
		return billDTO;
	}
}
