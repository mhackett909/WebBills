package com.projects.bills.Controllers;

import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Services.BillService;
import com.projects.bills.Services.EntryService;
import com.projects.bills.Services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
public class EntryController {
	private final EntryService entryService;
	private final JwtService jwtService;

	@Autowired
	public EntryController(EntryService entryService, BillService billService, JwtService jwtService) {
		this.entryService = entryService;
		this.jwtService = jwtService;
	}

	@GetMapping("/api/v1/entries")
	public ResponseEntity<List<EntryDTO>> getEntries(
			@RequestHeader("Authorization") String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		String userName = jwtService.validateJwt(token).getSubject();

		List<EntryDTO> entries = entryService.getEntries(userName);
		return new ResponseEntity<>(entries, HttpStatus.OK);
	}

	@GetMapping("/api/v1/entries/{id}")
	public ResponseEntity<EntryDTO> getEntryById(@PathVariable Long id) {
		return entryService.getEntryDtoById(id)
				.map(entryDTO -> new ResponseEntity<>(entryDTO, HttpStatus.OK))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + id));
	}

	@PostMapping("api/v1/entries/new")
	public ResponseEntity<EntryDTO> addEntry(@RequestBody EntryDTO entryDTO) {
		validateDTO(entryDTO, false);

		EntryDTO savedEntry = entryService.saveEntry(entryDTO, false);
		return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);
	}

	@PutMapping("api/v1/entries/edit")
	public ResponseEntity<EntryDTO> editEntry(@RequestBody EntryDTO entryDTO) {
		validateDTO(entryDTO, true);

		EntryDTO updatedEntry = entryService.saveEntry(entryDTO, true);
		return new ResponseEntity<>(updatedEntry, HttpStatus.OK);
	}

	// TODO Recycle entry

	// TODO Get Stats

	// TODO Export to CSV

	private void validateDTO(EntryDTO entryDTO, boolean validateEntryId) {
		if (validateEntryId && entryDTO.getEntryId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry id is required");
		}
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

		try {
			FlowType.fromType(entryDTO.getFlow());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid flow type: " + entryDTO.getFlow());
		}
	}
}
