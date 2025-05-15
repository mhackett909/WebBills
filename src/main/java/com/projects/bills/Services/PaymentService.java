package com.projects.bills.Services;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final EntryService entryService;
	
	@Autowired
	public PaymentService(PaymentRepository paymentRepository, EntryService entryService) {
		this.paymentRepository = paymentRepository;
		this.entryService = entryService;
	}

	public List<PaymentDTO> getPayments(Long entryId) {
		List<Payment> payments = paymentRepository.findAllByEntryId(entryId);
		ArrayList<PaymentDTO> paymentList = new ArrayList<>();
		for (Payment payment : payments) {
			PaymentDTO paymentDTO = mapToPaymentDTO(payment);
			paymentList.add(paymentDTO);
		}
		return paymentList;
	}

	public PaymentDTO createPayment(PaymentDTO paymentDTO) {
		Payment payment = new Payment();

		buildPaymentFromDTO(paymentDTO, payment);

		Payment savedPayment = paymentRepository.save(payment);

		return mapToPaymentDTO(savedPayment);
	}

	public PaymentDTO updatePayment(PaymentDTO paymentDTO) {
		Payment payment = paymentRepository.findById(paymentDTO.getPaymentId())
				.orElseThrow(() -> new IllegalArgumentException("Payment not found"));

		buildPaymentFromDTO(paymentDTO, payment);

		Payment savedPayment = paymentRepository.save(payment);

		return mapToPaymentDTO(savedPayment);
	}

	private void buildPaymentFromDTO(PaymentDTO paymentDTO, Payment payment) {
		payment.setDate(paymentDTO.getDate());
		payment.setAmount(paymentDTO.getAmount());
		payment.setType(paymentDTO.getType());
		payment.setMedium(paymentDTO.getMedium());
		payment.setNotes(paymentDTO.getNotes());

		Optional<Entry> entry = entryService.getEntryById(paymentDTO.getEntryId());
		if (entry.isEmpty()) {
			throw new IllegalArgumentException("Entry not found");
		}
		payment.setEntry(entry.get());
	}

	public PaymentDTO mapToPaymentDTO(Payment payment) {
		if (payment == null) {
			return null;
		}
		PaymentDTO dto = new PaymentDTO();
		dto.setPaymentId(payment.getId());
		dto.setDate(payment.getDate());
		dto.setAmount(payment.getAmount());
		dto.setType(payment.getType());
		dto.setMedium(payment.getMedium());
		dto.setNotes(payment.getNotes());
		if (payment.getEntry() != null) {
			dto.setEntryId(payment.getEntry().getId());
		}
		return dto;
	}
}