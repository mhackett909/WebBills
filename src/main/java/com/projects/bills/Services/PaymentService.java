package com.projects.bills.Services;
import com.projects.bills.Constants.Strings;
import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.PaymentMapper;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
	private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentRepository paymentRepository;
	private final EntryService entryService;
	private final PaymentMapper paymentMapper;

	@Autowired
	public PaymentService(PaymentRepository paymentRepository, EntryService entryService, PaymentMapper paymentMapper) {
		this.paymentRepository = paymentRepository;
		this.entryService = entryService;
        this.paymentMapper = paymentMapper;
    }

	public PaymentDTOList getPayments(Long entryId, String userName) {
		try {
			validateUserAccess(entryId, userName);
		} catch (ResponseStatusException e) {
			logger.error("User {} does not have access to entry {}", userName, entryId, e);
			throw e; // Re-throw the exception to maintain the error response
		}
		logger.info("Fetching payments for entryId={} by user={}", entryId, userName);
		List<Payment> payments = paymentRepository.findAllByEntryIdAndRecycleDateIsNullOrderByRecycleDateDesc(entryId);
		logger.info("Found {} payments for entryId={}", payments.size(), entryId);
		return paymentMapper.mapToDtoList(payments);
	}

	public PaymentDTO getPaymentById(Long paymentId, String userName) {
		Optional<Payment> payment = paymentRepository.findById(paymentId);
		if (payment.isEmpty()) {
			logger.error("Payment with ID {} not found", paymentId);
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					String.format(Exceptions.PAYMENT_NOT_FOUND, paymentId)
			);
		}

		try {
			validateUserAccess(payment.get().getEntry().getId(), userName);
		} catch (ResponseStatusException e) {
			logger.error("User {} does not have access to payment {}", userName, paymentId, e);
			throw e; // Re-throw the exception to maintain the error response
		}

		logger.info("Fetched payment with ID {} for entryId={} by user={}",
				paymentId,
				payment.get().getEntry().getId(),
				userName);

		return paymentMapper.mapToPaymentDTO(payment.get());
	}

	public PaymentDTO createPayment(PaymentDTO paymentDTO, String userName) {
		Entry entry;
		try {
			entry = validateUserAccess(paymentDTO.getEntryId(), userName);
		} catch (ResponseStatusException e) {
			logger.error("User {} does not have access to entry {}", userName, paymentDTO.getEntryId(), e);
			throw e; // Re-throw the exception to maintain the error response
		}

		if (!entry.getBill().getStatus()) {
			logger.error("Cannot add payment for archived entry {}", entry.getId());
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.CANNOT_ADD_PAYMENT_ARCHIVED);
		}

		logger.info("Creating payment for entryId={} by user={}", paymentDTO.getEntryId(), userName);

		Payment payment = paymentMapper.buildPaymentFromDTO(paymentDTO, new Payment(), entry);

		Payment savedPayment = paymentRepository.save(payment);

		entryService.calculatePaid(entry);

		logger.info("Payment created successfully with ID {}", savedPayment.getPaymentId());
		return paymentMapper.mapToPaymentDTO(savedPayment);
	}

	public PaymentDTO updatePayment(PaymentDTO paymentDTO, String filter, String userName) {
		Entry entry;
		try {
			entry = validateUserAccess(paymentDTO.getEntryId(), userName);
		} catch (ResponseStatusException e) {
			logger.error("User {} does not have access to entry {}", userName, paymentDTO.getEntryId(), e);
			throw e; // Re-throw the exception to maintain the error response
		}

		if (!entry.getBill().getStatus() && !Strings.EDIT_BYPASS.equalsIgnoreCase(filter)) {
			logger.error("Cannot update payment for archived entry {}", entry.getId());
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.CANNOT_UPDATE_PAYMENT_ARCHIVED);
		}

		Optional<Payment> optionalPayment = paymentRepository.findById(paymentDTO.getPaymentId());
		if (optionalPayment.isEmpty()) {
			logger.error("Payment with ID {} not found", paymentDTO.getPaymentId());
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					String.format(Exceptions.PAYMENT_NOT_FOUND, paymentDTO.getPaymentId())
			);
		}

		if (optionalPayment.get().getRecycleDate() != null && !Strings.EDIT_BYPASS.equalsIgnoreCase(filter)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.CANNOT_UPDATE_RECYCLED_PAYMENT);
		}

		Payment payment = paymentMapper.buildPaymentFromDTO(paymentDTO, optionalPayment.get(), entry);

		Payment savedPayment = paymentRepository.save(payment);

		entryService.calculatePaid(entry);

		logger.info("Payment updated successfully with ID {}", savedPayment.getPaymentId());
		return paymentMapper.mapToPaymentDTO(savedPayment);
	}

	private Entry validateUserAccess(long entryId, String userName) {
		Entry entry = entryService.getEntryById(entryId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						String.format(Exceptions.ENTRY_NOT_FOUND, entryId))
				);

		User user = entry.getBill().getUser();

		if(!user.getUsername().equalsIgnoreCase(userName)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_ENTRY);
		}

		return entry;
	}
}

