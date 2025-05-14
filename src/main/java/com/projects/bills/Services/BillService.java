package com.projects.bills.Services;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
			BillDTO billDTO = new BillDTO();
			billDTO.setId(bill.getBillId());
			billDTO.setName(bill.getName());
			billDTO.setStatus(bill.getStatus());
			billList.add(billDTO);
		}
		return billList;
	}
	public BillDTO getBill(String name) {
		Bill bill = billRepository.findByName(name).orElse(null);
		if (bill == null) return null;
		BillDTO billDTO = new BillDTO();
		billDTO.setId(bill.getBillId());
		billDTO.setName(bill.getName());
		billDTO.setStatus(bill.getStatus());
		return billDTO;
	}
	
	public void saveBill(Bill bill) {
		billRepository.save(bill);
	}
	
	public void delBill(String name) { billRepository.deleteByName(name); }
}
