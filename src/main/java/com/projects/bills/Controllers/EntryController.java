package com.projects.bills.Controllers;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.EntryDTO;
import com.projects.bills.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EntryController {
	private final EntryService entryService;
	
	@Autowired
	public EntryController(EntryService entryService) {
		this.entryService = entryService;
	}

	// TODO filters
	@GetMapping("/api/v1/entries")
	public List<EntryDTO> getEntries() { return entryService.getEntries(); }

	// TODO Get entry by id

	// TODO finish
	@GetMapping("api/v1/new")
	public void addEntry() {
		Bill test = new Bill();
		test.setName("Test3");
		test.setStatus(true);

		Entry entry = new Entry();
		entry.setBill(test);
		entry.setDate(Date.valueOf("1987-10-13"));
		entry.setAmount(new BigDecimal("10.21"));
		entry.setStatus(false);
		entry.setServices("services lol");
		entryService.saveEntry(entry);
	}

	// TODO Edit entry

	// TODO Delete entry

	// TODO Get Stats

	// TODO Export to CSV
}
