package com.projects.bills.Controllers;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.BillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.projects.bills.Services.BillService;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class BillController {
	private final BillService billService;
	
	@Autowired
	public BillController(BillService billService) {
		this.billService = billService;
	}
	
	@GetMapping("/api/v1/bills")
	public List<BillDTO> getBills() { return billService.getBills(); }
	
	@GetMapping("/api/v1/bills/{name}")
	public BillDTO getBills(@PathVariable("name") String name) {
		BillDTO bill = billService.getBills(name);
		if (bill == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Bill name: "+name);
		return bill;
	}
	
	@PostMapping("/api/v1/bills")
	public void newBill(@RequestBody BillDTO billTransfer) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");
		if (billTransfer.getStatus() == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill status");
		Bill bill = new Bill();
		bill.setName(billTransfer.getName());
		bill.setStatus(billTransfer.getStatus());
		billService.saveBill(bill);
	}
	
	@DeleteMapping("/api/v1/bills")
	public void delBill(@RequestBody BillDTO billTransfer) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");
		if (billService.getBills(billTransfer.getName()) == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill does not exist: "+billTransfer.getName());
		billService.delBill(billTransfer.getName());
	}
}
