package com.projects.bills.Controllers;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.PaymentDTO;
import com.projects.bills.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public List<PaymentDTO> getPayments() {
		return paymentService.getPayments();
	}

}
