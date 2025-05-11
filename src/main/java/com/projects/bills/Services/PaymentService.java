package com.projects.bills.Services;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.PaymentDTO;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	
	@Autowired
	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}
	//In reality will get payments by entry ID
	public List<PaymentDTO> getPayments() {
		List<Payment> payments = paymentRepository.findAll();
		ArrayList<PaymentDTO> paymentList = new ArrayList<>();
		for (Payment payment : payments) {
			PaymentDTO paymentDTO = new PaymentDTO();
			paymentDTO.setPaymentId(payment.getId());
			paymentDTO.setDate(payment.getDate());
			paymentDTO.setAmount(payment.getAmount());
			paymentDTO.setType(payment.getType());
			paymentDTO.setMedium(payment.getMedium());
			paymentDTO.setNotes(payment.getNotes());
			paymentList.add(paymentDTO);
		}
		return paymentList;
	}
	public void savePayment(Payment payment) {
		paymentRepository.save(payment);
	}
}
