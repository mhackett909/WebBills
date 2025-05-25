package com.projects.bills.Controllers;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class PaymentController {
	private final PaymentService paymentService;

	@Autowired
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTOList> getPayments(
			@RequestParam Long entryId,
			@AuthenticationPrincipal UserDetails user
	) {
		if (entryId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry ID is required");
		}
		PaymentDTOList paymentDTOS = paymentService.getPayments(entryId, user.getUsername());
		return new ResponseEntity<>(paymentDTOS, HttpStatus.OK);
	}

	@GetMapping("/api/v1/payments/{id}")
	public ResponseEntity<PaymentDTO> getPaymentById(
			@PathVariable("id") Long paymentId,
			@AuthenticationPrincipal UserDetails user) {
		if (paymentId == null || paymentId == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID is required");
		}
		PaymentDTO paymentDTO = paymentService.getPaymentById(paymentId, user.getUsername());

		return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
	}

	@PostMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTO> createPayment(
			@RequestBody PaymentDTO paymentDTO,
			@AuthenticationPrincipal UserDetails user) {
		verifyPaymentDTO(paymentDTO, false);

		PaymentDTO responseDTO = paymentService.createPayment(paymentDTO, user.getUsername());
		return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
	}

	@PutMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTO> updatePayment(
			@RequestBody PaymentDTO paymentDTO,
			@RequestParam(required = false) String filter,
			@AuthenticationPrincipal UserDetails user) {
		verifyPaymentDTO(paymentDTO, true);

		PaymentDTO responseDTO = paymentService.updatePayment(paymentDTO, filter, user.getUsername());
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
	}
}