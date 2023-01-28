package com.projects.bills.Services;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	
	@Autowired
	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}
	public List<Payment> getPayments() {
		return paymentRepository.findAll();
	}
	public void savePayment(Payment payment) {
		paymentRepository.save(payment);
	}
}
