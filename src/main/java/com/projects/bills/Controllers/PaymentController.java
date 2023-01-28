package com.projects.bills.Controllers;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public class PaymentController {
	private final PaymentService paymentService;
	
	@Autowired
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	
	@GetMapping("/api/v1/payments")
	public List<Payment> getPayments() {
		return paymentService.getPayments();
	}
	/**
	@GetMapping("api/v1/new")
	public void add() {
		Payment payment = new Payment();

		paymentService.savePayment(payment);
	}
	**/
}
