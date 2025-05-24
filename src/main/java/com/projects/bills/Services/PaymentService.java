package com.projects.bills.Services;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final EntryService entryService;
	
	@Autowired
	public PaymentService(PaymentRepository paymentRepository, EntryService entryService) {
		this.paymentRepository = paymentRepository;
		this.entryService = entryService;
	}

	public List<PaymentDTO> getPayments(Long entryId, String userName) {
		validateUserAccess(entryId, userName);

		List<Payment> payments = paymentRepository.findAllByEntryIdAndRecycleDateIsNull(entryId);
		ArrayList<PaymentDTO> paymentList = new ArrayList<>();
		for (Payment payment : payments) {
			PaymentDTO paymentDTO = mapToPaymentDTO(payment);
			paymentList.add(paymentDTO);
		}
		return paymentList;
	}

	public PaymentDTO getPaymentById(Long paymentId, String userName) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

		validateUserAccess(payment.getEntry().getId(), userName);

		return mapToPaymentDTO(payment);
	}

	public PaymentDTO createPayment(PaymentDTO paymentDTO, String userName) {
		Payment payment = new Payment();

		Entry entry = validateUserAccess(paymentDTO.getEntryId(), userName);

		if (!entry.getBill().getStatus()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot add a payment for entry linked to an archived entity");
		}

		buildPaymentFromDTO(paymentDTO, payment, entry);

		Payment savedPayment = paymentRepository.save(payment);

		calculatePaid(entry);

		return mapToPaymentDTO(savedPayment);
	}

	public PaymentDTO updatePayment(PaymentDTO paymentDTO, String filter, String userName) {
		Entry entry = validateUserAccess(paymentDTO.getEntryId(), userName);

		if (!entry.getBill().getStatus()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update payment for entry linked to an archived entity");
		}

		Payment payment = paymentRepository.findById(paymentDTO.getPaymentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

		if (payment.getRecycleDate() != null && !"bypass".equalsIgnoreCase(filter)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update a recycled payment");
		}

		buildPaymentFromDTO(paymentDTO, payment, entry);

		Payment savedPayment = paymentRepository.save(payment);

		calculatePaid(entry);

		return mapToPaymentDTO(savedPayment);
	}

	private void calculatePaid(Entry entry) {
		BigDecimal entryAmount = entry.getAmount();
		BigDecimal paidAmount = paymentRepository.sumAmountByEntryIdAndRecycleDateIsNull(entry.getId());

		// Validation for this entry has already occurred
		String entryUser = entry.getUser().getUsername();

		EntryDTO entryDTO = entryService.getEntryDtoById(entry.getId(), null, entryUser)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry DTO not found"));

		// The entry is paid if the paid amount is greater than or equal to the entry amount
        entryDTO.setStatus(entryAmount.compareTo(paidAmount) <= 0);

		// The entry is overpaid if the paid amount is greater than the entry amount
		entryDTO.setOverpaid(paidAmount.compareTo(entryAmount) > 0);

		entryDTO.setFlow(FlowType.fromName(entryDTO.getFlow()));

		entryService.saveEntry(entryDTO, true, null, entryUser);
	}

	private void buildPaymentFromDTO(PaymentDTO paymentDTO, Payment payment, Entry entry) {
		payment.setDate(Date.valueOf(paymentDTO.getDate()));
		payment.setAmount(paymentDTO.getAmount());
		payment.setType(paymentDTO.getType());
		payment.setMedium(paymentDTO.getMedium());
		payment.setNotes(paymentDTO.getNotes());
		payment.setEntry(entry);
		payment.setRecycleDate(paymentDTO.getRecycle() ? LocalDateTime.now() : null);
	}

	private PaymentDTO mapToPaymentDTO(Payment payment) {
		if (payment == null) {
			return null;
		}
		PaymentDTO dto = new PaymentDTO();
		dto.setPaymentId(payment.getPaymentId());
		dto.setDate(payment.getDate() != null ? payment.getDate().toLocalDate() : null);
		dto.setAmount(payment.getAmount());
		dto.setType(payment.getType());
		dto.setMedium(payment.getMedium());
		dto.setNotes(payment.getNotes());
		dto.setRecycle(payment.getRecycleDate() != null);
		if (payment.getEntry() != null) {
			dto.setEntryId(payment.getEntry().getId());
		}
		return dto;
	}

	private Entry validateUserAccess(long entryId, String userName) {
		Entry entry = entryService.getEntryById(entryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

		User user = entry.getBill().getUser();

		if(!user.getUsername().equalsIgnoreCase(userName)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this entry");
		}

		return entry;
	}
}