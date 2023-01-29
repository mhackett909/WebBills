package com.projects.bills;

import com.projects.bills.Controllers.BillController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes={ BillController.class})
//@AutoConfigureMockMVC(addFilters = false)
@WebMvcTest
public class JUnitTestBillController {
	//Test for new bill
	//Test for del bill
}
