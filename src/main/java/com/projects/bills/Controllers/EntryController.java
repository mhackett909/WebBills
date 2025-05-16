package com.projects.bills.Controllers;

import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Services.BillService;
import com.projects.bills.Services.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<List<EntryDTO>> getEntries() {
		List<EntryDTO> entries = entryService.getEntries();
		return new ResponseEntity<>(entries, HttpStatus.OK);
	}

	@GetMapping("/api/v1/entries/{id}")
	public ResponseEntity<EntryDTO> getEntryById(@PathVariable Long id) {
		return entryService.getEntryDtoById(id)
				.map(entryDTO -> new ResponseEntity<>(entryDTO, HttpStatus.OK))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + id));
	}

	@PostMapping("api/v1/new")
	public ResponseEntity<EntryDTO> addEntry(@RequestBody EntryDTO entryDTO) {
		if (entryDTO.getBillId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill id is required");
		}
		if (entryDTO.getDate() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
		}
		if (entryDTO.getAmount() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
		}
		if (entryDTO.getFlow() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flow is required");
		}
		if (entryDTO.getStatus() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
		}

		Bill bill = billService.getBillEntityById(entryDTO.getBillId());
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill not found for name: " + entryDTO.getName());
		}

		FlowType type;
		try {
			type = FlowType.fromType(entryDTO.getFlow());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid flow type: " + entryDTO.getFlow());
		}

		EntryDTO savedEntry = entryService.saveEntry(entryDTO, bill, type);
		return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);
	}

	// TODO Edit entry

	// TODO Delete entry

	// TODO Get Stats

	// TODO Export to CSV
}
