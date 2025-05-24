package com.projects.bills.Controllers;
import com.projects.bills.DTOs.BillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
	public ResponseEntity<List<BillDTO>> getBills(
			@RequestParam(required = false) String filter,
			@AuthenticationPrincipal UserDetails user) {

		List<BillDTO> bills = billService.getBillDtoList(filter, user.getUsername());
		return new ResponseEntity<>(bills, HttpStatus.OK);
	}

	@GetMapping("/api/v1/bills/{id}")
	public ResponseEntity<BillDTO> getBillsById(@PathVariable("id") Long id,
												@RequestParam(required = false) String filter,
												@AuthenticationPrincipal UserDetails user) {
		BillDTO bill = billService.getBill(id, filter, user.getUsername());
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill does not exist by id: " + id);
		}

		return new ResponseEntity<>(bill, HttpStatus.OK);
	}

	@PostMapping("/api/v1/bills")
	public ResponseEntity<BillDTO> newBill(@RequestBody BillDTO billTransfer,
										   @AuthenticationPrincipal UserDetails user) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");

		if (billTransfer.getStatus() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill status is required");
		}

		BillDTO savedBill = billService.saveBill(billTransfer, false, user.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(savedBill);
	}

	@PutMapping("/api/v1/bills")
	public ResponseEntity<BillDTO> editBill(@RequestBody BillDTO billTransfer,
											@AuthenticationPrincipal UserDetails user) {
		if (billTransfer.getId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill id is required");
		}
		if (billTransfer.getName() == null || billTransfer.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bill name");
		}
		if (billTransfer.getStatus() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill status is required");
		}
		if (billTransfer.getRecycle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill recycle status is required");
		}

		BillDTO updatedBill = billService.saveBill(billTransfer, true, user.getUsername());
		return ResponseEntity.ok(updatedBill);
	}
}
