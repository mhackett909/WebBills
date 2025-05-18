package com.projects.bills.Controllers;
import com.projects.bills.DTOs.BillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<List<BillDTO>> getBills(@RequestParam(required = false) String filter) {
		List<BillDTO> bills = billService.getBills(filter);
		return new ResponseEntity<>(bills, HttpStatus.OK);
	}

	@GetMapping("/api/v1/bills/{id}")
	public ResponseEntity<BillDTO> getBillsById(@PathVariable("id") Long id) {
		BillDTO bill = billService.getBill(id);
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill does not exist by id: " + id);
		}

		return new ResponseEntity<>(bill, HttpStatus.OK);
	}

	@PostMapping("/api/v1/bills")
	public ResponseEntity<BillDTO> newBill(@RequestBody BillDTO billTransfer) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");

		BillDTO savedBill = billService.saveBill(billTransfer);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedBill);
	}

	@DeleteMapping("/api/v1/bills")
	public ResponseEntity<BillDTO> delBill(@RequestBody BillDTO billTransfer) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");

		BillDTO deleted = billService.delBill(billTransfer.getName());
		if (deleted == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill does not exist: " + billTransfer.getName());

		return new ResponseEntity<>(deleted, HttpStatus.OK);
	}

	// TODO Edit bill (this includes archiving)

	// TODO Recycle Bin
}
