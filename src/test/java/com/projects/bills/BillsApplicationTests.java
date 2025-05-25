package com.projects.bills;

import com.projects.bills.Repositories.BillRepository;
import com.projects.bills.Repositories.EntryRepository;
import com.projects.bills.Repositories.PaymentRepository;
import com.projects.bills.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BillsApplicationTests {

	@Autowired
	private EntryRepository entryRepository;

	@Autowired
	private BillRepository billRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
		assertNotNull(entryRepository);
		assertNotNull(billRepository);
		assertNotNull(paymentRepository);
		assertNotNull(userRepository);
	}
}
