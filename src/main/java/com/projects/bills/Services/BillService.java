package com.projects.bills.Services;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {
	private final BillRepository billRepository;
	
	@Autowired
	public BillService(BillRepository billRepository) {
		this.billRepository = billRepository;
	}

	//Need to take in a boolean for inactive billers
	public List<BillDTO> getBills() {
		List<Bill> bills = billRepository.findAll();
		ArrayList<BillDTO> billList = new ArrayList<>();
		for (Bill bill : bills) {
			BillDTO billDTO = mapToDTO(bill);
			billList.add(billDTO);
		}
		return billList;
	}

	public BillDTO getBill(String name) {
		Bill bill = billRepository.findByName(name).orElse(null);
		if (bill == null) return null;
		return mapToDTO(bill);
	}
	
	public BillDTO saveBill(Bill bill) {
		Bill updatedBill = billRepository.save(bill);
		return mapToDTO(updatedBill);
	}

	public BillDTO delBill(String name) {
		Optional<Bill> deletedBill = billRepository.deleteByName(name);
        return deletedBill.map(BillService::mapToDTO).orElse(null);
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
