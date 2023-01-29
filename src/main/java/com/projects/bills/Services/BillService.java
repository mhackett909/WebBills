package com.projects.bills.Services;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.BillDTO;
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
		for (Bill bill : bills)
			billList.add(new BillDTO(bill.getName(), bill.getStatus()));
		return billList;
	}
	public BillDTO getBills(String name) {
		Bill bill = billRepository.findById(name).orElse(null);
		if (bill == null) return null;
		return new BillDTO(bill.getName(), bill.getStatus());
	}
	
	public void saveBill(Bill bill) {
		billRepository.save(bill);
	}
	
	public void delBill(String name) { billRepository.deleteById(name); }
}
