package com.projects.bills.Controllers;

import com.projects.bills.Entities.Entry;
import com.projects.bills.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class EntryController {
	private final EntryService entryService;
	
	@Autowired
	public EntryController(EntryService entryService) {
		this.entryService = entryService;
	}
	
	@GetMapping("/api/v1/entries")
	public List<Entry> getEntries() { return entryService.getEntries(); }
	/**
	@GetMapping("api/v1/new")
	public void add() {
		Entry entry = new Entry();
		Bill test = new Bill();
		test.setName("Test3");
		test.setStatus(false);
		entry.setBill(test);
		entry.setDate(Date.valueOf("1987-10-13"));
		entry.setAmount(new BigDecimal(10.21));
		entry.setStatus(4);
		entry.setServices("services lol");
		entryService.saveEntry(entry);
	}
	**/
}
