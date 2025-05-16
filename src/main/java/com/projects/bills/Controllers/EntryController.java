package com.projects.bills.Controllers;

import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Services.BillService;
import com.projects.bills.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;


@RestController
public class EntryController {
	private final EntryService entryService;
	private final BillService billService;
	
	@Autowired
	public EntryController(EntryService entryService, BillService billService) {
		this.entryService = entryService;
        this.billService = billService;
    }

	// TODO filters
	@GetMapping("/api/v1/entries")
	public List<EntryDTO> getEntries() { return entryService.getEntries(); }

	// TODO Get entry by id

	@PostMapping("api/v1/new")
	public void addEntry(@RequestBody EntryDTO entryDTO) {
		if (entryDTO.getName() == null || entryDTO.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill name is required");
		}
		if (entryDTO.getDate() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
		}
		if (entryDTO.getAmount() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
		}
		if (entryDTO.getStatus() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
		}

		BillDTO billDTO = billService.getBill(entryDTO.getName());
		if (billDTO == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill not found for name: " + entryDTO.getName());
		}

		Bill bill = new Bill();
		bill.setBillId(billDTO.getId());
		bill.setName(billDTO.getName());

		Entry entry = new Entry();
		entry.setBill(bill);
		entry.setDate(entryDTO.getDate());
		entry.setAmount(entryDTO.getAmount());
		entry.setStatus(entryDTO.getStatus());
		entry.setServices(entryDTO.getServices());

		entryService.saveEntry(entry);
	}

	// TODO Edit entry

	// TODO Delete entry

	// TODO Get Stats

	// TODO Export to CSV
}
