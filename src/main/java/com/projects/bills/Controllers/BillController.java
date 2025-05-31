package com.projects.bills.Controllers;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.DTOs.BillDTOList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.projects.bills.Services.BillService;
import org.springframework.web.server.ResponseStatusException;
import com.projects.bills.Constants.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class BillController {
	private static final Logger logger = LoggerFactory.getLogger(BillController.class);
	private final BillService billService;

	@Autowired
	public BillController(BillService billService) {
		this.billService = billService;
	}

	@GetMapping("/api/v1/bills")
	public ResponseEntity<BillDTOList> getBills(
			@RequestParam(required = false) String filter,
			@AuthenticationPrincipal UserDetails user) {

		BillDTOList billDTOList = billService.getBillDtoList(filter, user.getUsername());
		return new ResponseEntity<>(billDTOList, HttpStatus.OK);
	}

	@GetMapping("/api/v1/bills/{id}")
	public ResponseEntity<BillDTO> getBillsById(@PathVariable("id") Long id,
												@RequestParam(required = false) String filter,
												@AuthenticationPrincipal UserDetails user) {
		logger.info("Fetching bill with id {} for user {} with filter: {}", id, user.getUsername(), filter);
		BillDTO bill = billService.getBill(id, filter, user.getUsername());
		if (bill == null) {
			logger.error("Bill with id {} not found for user {}", id, user.getUsername());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(Exceptions.BILL_NOT_FOUND, id));
		}

		return new ResponseEntity<>(bill, HttpStatus.OK);
	}

	@PostMapping("/api/v1/bills")
	public ResponseEntity<BillDTO> newBill(@RequestBody BillDTO billTransfer,
										   @AuthenticationPrincipal UserDetails user) {
		if (billTransfer.getName() == null || billTransfer.getName().isBlank()) {
			logger.error("Invalid bill name provided by user {}", user.getUsername());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.INVALID_BILL_NAME);
		}

		if (billTransfer.getStatus() == null) {
			logger.error("Bill status is required but not provided by user {}", user.getUsername());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.BILL_STATUS_REQUIRED);
		}

		BillDTO savedBill = billService.saveBill(billTransfer, false, user.getUsername());
		return ResponseEntity.status(HttpStatus.CREATED).body(savedBill);
	}

	@PutMapping("/api/v1/bills")
	public ResponseEntity<BillDTO> editBill(@RequestBody BillDTO billTransfer,
											@AuthenticationPrincipal UserDetails user) {
		if (billTransfer.getId() == 0) {
			logger.error("Bill ID is required for update but not provided by user {}", user.getUsername());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.BILL_ID_REQUIRED);
		}
		if (billTransfer.getName() == null || billTransfer.getName().isBlank()) {
			logger.error("Invalid bill name provided by user {}", user.getUsername());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.INVALID_BILL_NAME);
		}
		if (billTransfer.getStatus() == null) {
			logger.error("Bill status is required but not provided by user {}", user.getUsername());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.BILL_STATUS_REQUIRED);
		}

		BillDTO updatedBill = billService.saveBill(billTransfer, true, user.getUsername());
		return ResponseEntity.ok(updatedBill);
	}
}
