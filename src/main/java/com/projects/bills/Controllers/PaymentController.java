package com.projects.bills.Controllers;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
public class PaymentController {
	private final PaymentService paymentService;

	@Autowired
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping("/api/v1/payments")
	public ResponseEntity<List<PaymentDTO>> getPayments(@RequestParam Long entryId) {
		if (entryId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry ID is required");
		}
		List<PaymentDTO> paymentDTOS = paymentService.getPayments(entryId);
		return new ResponseEntity<>(paymentDTOS, HttpStatus.OK);
	}

	@GetMapping("/api/v1/payments/{id}")
	public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable("id") Long paymentId) {
		if (paymentId == null || paymentId == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID is required");
		}
		PaymentDTO paymentDTO = paymentService.getPaymentById(paymentId);

		return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
	}

	@PostMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
		verifyPaymentDTO(paymentDTO, false);

		PaymentDTO responseDTO = paymentService.createPayment(paymentDTO);
		return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
	}

	@PutMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTO> updatePayment(@RequestBody PaymentDTO paymentDTO, @RequestParam(required = false) String filter) {
		verifyPaymentDTO(paymentDTO, true);

		PaymentDTO responseDTO = paymentService.updatePayment(paymentDTO, filter);
		return new ResponseEntity<>(responseDTO, HttpStatus.OK);
	}

	private void verifyPaymentDTO(PaymentDTO paymentDTO, boolean verifyExisting) {
		if (verifyExisting && paymentDTO.getPaymentId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID is required for update");
		}

		if (paymentDTO.getEntryId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry ID is required");
		}

		if (paymentDTO.getAmount() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
		}

		if (paymentDTO.getDate() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
		}

		if (paymentDTO.getType() == null || paymentDTO.getType().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type is required");
		}

		if (paymentDTO.getMedium() == null || paymentDTO.getMedium().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medium is required");
		}

		if (paymentDTO.getRecycle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recycle is required");
		}
	}
}