package com.projects.bills.Controllers;
import com.projects.bills.Entities.Bill;
import com.projects.bills.DTOs.BillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.projects.bills.Services.BillService;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
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
		if (name == null || name.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Bill name");
		}

		BillDTO bill = billService.getBill(name);
		if (bill == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill does not exist: "+name);

		return bill;
	}

	@PostMapping("/api/v1/bills")
	public BillDTO newBill(@RequestBody BillDTO billTransfer) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");

		Bill bill = new Bill();
		bill.setName(billTransfer.getName());
		bill.setStatus(billTransfer.getStatus()); // Default to active
		return billService.saveBill(bill);
	}

	@DeleteMapping("/api/v1/bills")
	public BillDTO delBill(@RequestBody BillDTO billTransfer) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");

		BillDTO deleted = billService.delBill(billTransfer.getName());
		if (deleted == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill does not exist: " + billTransfer.getName());

		return deleted;
	}

	// TODO Edit bill (this includes archiving)

	// TODO Recycle Bin
}
