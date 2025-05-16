package com.projects.bills.Controllers;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
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
	public List<PaymentDTO> getPayments(@RequestParam Long entryId) {
		if (entryId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry ID is required");
		}
		return paymentService.getPayments(entryId);
	}

	@PostMapping("/api/v1/payments")
	public PaymentDTO createPayment(@RequestBody PaymentDTO paymentDTO) {
		verifyPaymentDTO(paymentDTO);

		return paymentService.createPayment(paymentDTO);
	}

	@PutMapping("/api/v1/payments")
	public PaymentDTO updatePayment(@RequestBody PaymentDTO paymentDTO) {
		verifyPaymentDTO(paymentDTO);

		return paymentService.updatePayment(paymentDTO);
	}

	private void verifyPaymentDTO(PaymentDTO paymentDTO) {
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
	}
}