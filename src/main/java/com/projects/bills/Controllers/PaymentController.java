package com.projects.bills.Controllers;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Services.PaymentService;
import com.projects.bills.Constants.Exceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class PaymentController {
	private final PaymentService paymentService;
	private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Autowired
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTOList> getPayments(
			@RequestParam Long entryId,
			@AuthenticationPrincipal UserDetails user
	) {
		logger.info("Received getPayments request for entryId={} by user={}", entryId, user.getUsername());
		if (entryId == null) {
			logger.warn("Validation failed: Entry ID is required");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.ENTRY_ID_REQUIRED);
		}

		PaymentDTOList paymentDTOS = paymentService.getPayments(entryId, user.getUsername());
		return new ResponseEntity<>(paymentDTOS, HttpStatus.OK);
	}

	@GetMapping("/api/v1/payments/{id}")
	public ResponseEntity<PaymentDTO> getPaymentById(
			@PathVariable("id") Long paymentId,
			@AuthenticationPrincipal UserDetails user) {
		logger.info("Received getPaymentById request for paymentId={} by user={}", paymentId, user.getUsername());
		if (paymentId == null || paymentId == 0) {
			logger.warn("Payment ID is required for fetching payment details");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PAYMENT_ID_REQUIRED);
		}

		PaymentDTO paymentDTO = paymentService.getPaymentById(paymentId, user.getUsername());
		return new ResponseEntity<>(paymentDTO, HttpStatus.OK);
	}

	@PostMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTO> createPayment(
			@RequestBody PaymentDTO paymentDTO,
			@AuthenticationPrincipal UserDetails user) {
		logger.info("Received createPayment request by user={}", user.getUsername());
		try {
			verifyPaymentDTO(paymentDTO, false);
		} catch (ResponseStatusException e) {
			logger.error("Validation failed: {}", e.getReason());
			throw e;
		}

		PaymentDTO responseDTO = paymentService.createPayment(paymentDTO, user.getUsername());
		return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
	}

	@PutMapping("/api/v1/payments")
	public ResponseEntity<PaymentDTO> updatePayment(
			@RequestBody PaymentDTO paymentDTO,
			@RequestParam(required = false) String filter,
			@AuthenticationPrincipal UserDetails user) {
		logger.info("Received updatePayment request by user={}", user.getUsername());
		try {
			verifyPaymentDTO(paymentDTO, true);
		} catch (ResponseStatusException e) {
			logger.error("Validation failed: {}", e.getReason());
			throw e;
		}

		PaymentDTO responseDTO = paymentService.updatePayment(paymentDTO, filter, user.getUsername());
		return new ResponseEntity<>(responseDTO, HttpStatus.OK);
	}

	private void verifyPaymentDTO(PaymentDTO paymentDTO, boolean verifyExisting) {
		if (verifyExisting && paymentDTO.getPaymentId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PAYMENT_ID_REQUIRED);
		}

		if (paymentDTO.getEntryId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.ENTRY_ID_REQUIRED);
		}

		if (paymentDTO.getAmount() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PAYMENT_AMOUNT_REQUIRED);
		}

		if (paymentDTO.getDate() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PAYMENT_DATE_REQUIRED);
		}

		if (paymentDTO.getType() == null || paymentDTO.getType().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PAYMENT_TYPE_REQUIRED);
		}

		if (paymentDTO.getMedium() == null || paymentDTO.getMedium().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.PAYMENT_MEDIUM_REQUIRED);
		}
	}
}


