package com.projects.bills.Services;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

	public List<BillDTO> getBillDtoList(String filter, String userName) {
		Optional<User> realUser = userService.findByUsername(userName);
		if (realUser.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}

		List<BillDTO> billDtoList = new ArrayList<>();
		List<Bill> bills;

		if ("active".equalsIgnoreCase(filter)) {
			bills = billRepository.findAllByStatusAndUser(true, realUser.get());
		} else if ("inactive".equalsIgnoreCase(filter)) {
			bills = billRepository.findAllByStatusAndUser(false, realUser.get());
		} else {
			bills = billRepository.findAllByUser(realUser.get());
		}

		for (Bill bill : bills) {
			BillDTO billDTO = mapToDTO(bill);
			billDtoList.add(billDTO);
		}

		return billDtoList;
	}

	public List<Bill> getBills(String userName) {
		Optional<User> realUser = userService.findByUsername(userName);
		if (realUser.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}

		return billRepository.findAllByUser(realUser.get());
	}

	public BillDTO getBill(Long id) {
		Bill bill = billRepository.findById(id).orElse(null);
		if (bill == null) return null;
		return mapToDTO(bill);
	}

	public Bill getBillEntityById(Long id) {
		return billRepository.findById(id).orElse(null);
	}

	public BillDTO saveBill(BillDTO billTransfer, boolean existing) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userService.findByUsername(username);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}

		Bill bill;
		if (existing) {
			// Load existing bill by ID
			bill = billRepository.findById(billTransfer.getId()).orElse(null);
			if (bill == null) {
				throw new IllegalArgumentException("Bill not found with id: " + billTransfer.getId());
			}
		} else {
			bill = new Bill();
		}

		bill.setName(billTransfer.getName());
		bill.setStatus(billTransfer.getStatus());
		bill.setUser(user.get());

		Bill updatedBill = billRepository.save(bill);
		return mapToDTO(updatedBill);
	}

	public BillDTO delBill(String name) {
		Optional<Bill> bill = billRepository.findByName(name);
		bill.ifPresent(billRepository::delete);
		return bill.map(BillService::mapToDTO).orElse(null);
	}

	public static BillDTO mapToDTO(Bill bill) {
		if (bill == null) return null;
		BillDTO billDTO = new BillDTO();
		billDTO.setId(bill.getBillId());
		billDTO.setName(bill.getName());
		billDTO.setStatus(bill.getStatus());
		return billDTO;
	}
}
