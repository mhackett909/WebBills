package com.projects.bills.Controllers;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {
	private final PaymentService paymentService;
	
	@Autowired
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping("/api/v1/payments")
	public List<PaymentDTO> getPayments(@RequestParam Long entryId) {
		if (entryId == null) {
			throw new IllegalArgumentException("Entry ID is required");
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
			throw new IllegalArgumentException("Amount is required");
		}
		if (paymentDTO.getDate() == null) {
			throw new IllegalArgumentException("Date is required");
		}
		if (paymentDTO.getType() == null || paymentDTO.getType().isBlank()) {
			throw new IllegalArgumentException("Type is required");
		}
		if (paymentDTO.getMedium() == null || paymentDTO.getMedium().isBlank()) {
			throw new IllegalArgumentException("Medium is required");
		}
	}
}
