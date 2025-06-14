package com.projects.bills.Controllers;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Services.EntryService;
import com.projects.bills.Services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
public class EntryController {
	private static final Logger logger = LoggerFactory.getLogger(EntryController.class);

	private final EntryService entryService;
	private final StatsService statsService;

	@Autowired
	public EntryController(EntryService entryService, StatsService statsService) {
		this.entryService = entryService;
        this.statsService = statsService;
    }

	@GetMapping("/api/v1/entries")
	public ResponseEntity<EntryDTOList> getEntries(
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate,
			@RequestParam(required = false) Long invoiceNum,
			@RequestParam(required = false) List<String> partyList,
			@RequestParam(required = false) BigDecimal min,
			@RequestParam(required = false) BigDecimal max,
			@RequestParam(required = false) String flow,
			@RequestParam(required = false) String paid,
			@RequestParam(required = false) String archives,
			@RequestParam(required = false) Integer pageNum,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String sortField,
			@RequestParam(required = false) String sortOrder,
			@AuthenticationPrincipal UserDetails user) {

		EntryDTOList entryDTOList = entryService.getEntries(
				user.getUsername(), startDate, endDate, invoiceNum, partyList,
				min, max, flow, paid, archives, pageNum, pageSize,
				sortField, sortOrder
		);

		return new ResponseEntity<>(entryDTOList, HttpStatus.OK);
	}

	@GetMapping("/api/v1/entries/{id}")
	public ResponseEntity<EntryDTO> getEntryById(@PathVariable Long id,
												 @RequestParam(required = false) String filter,
												 @AuthenticationPrincipal UserDetails user) {
		return entryService.getEntryDtoById(id, filter, user.getUsername())
				.map(entryDTO -> new ResponseEntity<>(entryDTO, HttpStatus.OK))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + id));
	}

	@PostMapping("/api/v1/entries/new")
	public ResponseEntity<EntryDTO> addEntry(@RequestBody EntryDTO entryDTO,
											 @AuthenticationPrincipal UserDetails user) {
		try {
			validateDTO(entryDTO, false);
		} catch (ResponseStatusException e) {
			logger.error("Validation failed for entryDTO: {}", entryDTO, e);
			throw e; // Re-throw the exception to return the error response
		}

		EntryDTO savedEntry = entryService.saveEntry(entryDTO, false, null, user.getUsername());
		return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);
	}

	@PutMapping("/api/v1/entries/edit")
	public ResponseEntity<EntryDTO> editEntry(
			@RequestBody EntryDTO entryDTO,
			@RequestParam(required = false) String filter,
			@AuthenticationPrincipal UserDetails user) {
		try {
			validateDTO(entryDTO, true);
		} catch (ResponseStatusException e) {
			logger.error("Validation failed for entryDTO: {}", entryDTO, e);
			throw e; // Re-throw the exception to return the error response
		}

		EntryDTO updatedEntry = entryService.saveEntry(entryDTO, true, filter, user.getUsername());
		return new ResponseEntity<>(updatedEntry, HttpStatus.OK);
	}

	@GetMapping("/api/v1/entries/stats")
	public ResponseEntity<StatsDTO> getStats(
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate,
			@RequestParam(required = false) Long invoiceNum,
			@RequestParam(required = false) List<String> partyList,
			@RequestParam(required = false) BigDecimal min,
			@RequestParam(required = false) BigDecimal max,
			@RequestParam(required = false) String flow,
			@RequestParam(required = false) String paid,
			@RequestParam(required = false) String archives,
			@AuthenticationPrincipal UserDetails user
	) {

		StatsDTO statsDTO = statsService.getStats(
				user.getUsername(), startDate, endDate, invoiceNum, partyList, min, max, flow, paid, archives
		);

		return new ResponseEntity<>(statsDTO, HttpStatus.OK);
	}

	// TODO Export to CSV

	private void validateDTO(EntryDTO entryDTO, boolean validateEntryId) {
		if (validateEntryId && entryDTO.getEntryId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.ENTRY_ID_REQUIRED);
		}
		if (entryDTO.getBillId() == 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.BILL_ID_REQUIRED);
		}
		if (entryDTO.getDate() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.ENTRY_DATE_REQUIRED);
		}
		if (entryDTO.getAmount() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.ENTRY_AMOUNT_REQUIRED);
		}
		if (entryDTO.getFlow() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.FLOW_IS_REQUIRED);
		}

		try {
			FlowType.fromType(entryDTO.getFlow());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(Exceptions.INVALID_FLOW_TYPE, entryDTO.getFlow()));
		}
	}
}
