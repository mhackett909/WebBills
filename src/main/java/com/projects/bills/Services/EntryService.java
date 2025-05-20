package com.projects.bills.Services;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Repositories.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.Date;

@Service
public class EntryService {
	private final EntryRepository entryRepository;
	private final BillService billService;
	private final UserService userService;

	@Autowired
	public EntryService(EntryRepository entryRepository, BillService billService, UserService userService) {
		this.billService = billService;
		this.entryRepository = entryRepository;
        this.userService = userService;
    }

	public List<EntryDTO> getEntries(String userName) {
		Optional<User> realUser = userService.findByUsername(userName);
		if (realUser.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		List<Bill> userBills = billService.getBills(userName);

		List<Entry> entries = entryRepository.findAllByBillInAndRecycleDateIsNullOrderByDateDesc(userBills);

		ArrayList<EntryDTO> entryList = new ArrayList<>();
		for (Entry entry : entries) {
			EntryDTO entryDTO = mapToDTO(entry);
			entryList.add(entryDTO);
		}
		return entryList;
	}

	public Optional<EntryDTO> getEntryDtoById(Long id, String filter) {
		Optional<Entry> entry;
		if (!"bypass".equalsIgnoreCase(filter)) {
			entry = Optional.ofNullable(entryRepository.findByIdAndRecycleDateIsNull(id));
		} else {
			entry = entryRepository.findById(id);
		}
		if (entry.isPresent()) {
			User user = entry.get().getBill().getUser();
			String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
			if (!user.getUsername().equalsIgnoreCase(requestingUser)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this entry");
			}
		}
		return entry.map(this::mapToDTO);
	}

	protected Optional<Entry> getEntryById(Long id) {
		return entryRepository.findById(id);
	}

	public EntryDTO saveEntry(EntryDTO entryDTO, boolean existing, String filter) {
		Entry entry;
		if (existing) {
			// Check if entry exists
			Optional<Entry> existingEntry = getEntryById(entryDTO.getEntryId());
			if (existingEntry.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + entryDTO.getEntryId());
			}
			entry = existingEntry.get();

			String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
			if (!entry.getBill().getUser().getUsername().equalsIgnoreCase(requestingUser)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this invoice");
			}

			if (entry.getRecycleDate() != null && !"bypass".equalsIgnoreCase(filter)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a recycled entry");
			}

			if (!entry.getBill().getStatus()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update an entry linked to an archived party");
			}
		} else {
			entry = new Entry();
		}

		Bill bill = billService.getBillEntityById(entryDTO.getBillId());
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill not found for id: " + entryDTO.getBillId());
		}

		String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!bill.getUser().getUsername().equalsIgnoreCase(requestingUser)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this bill");
		}

		FlowType type = FlowType.fromType(entryDTO.getFlow());

		Entry mappedEntry = mapToEntity(entryDTO, entry, bill, type);

		Entry savedEntry = entryRepository.save(mappedEntry);
		return mapToDTO(savedEntry);
	}

	private boolean isArchived(Entry entry) {
		BillDTO bill = billService.getBill(entry.getBill().getBillId(), null);
		if (bill == null || bill.getStatus() == null) {
			return false;
		}
		// If bill's status is true, it is enabled (and therefore not archived)
		return !bill.getStatus();
	}

	private EntryDTO mapToDTO(Entry entry) {
		return new EntryDTO(
				entry.getId(),
				entry.getBill().getBillId(),
				entry.getBill().getName(), // Not using entry.getBill().getName() because it is legacy
				entry.getDate() != null ? entry.getDate().toLocalDate() : null,
				entry.getAmount(),
				entry.getStatus(),
				entry.getRecycleDate() != null,
				entry.getServices(),
				entry.getFlow(),
				isArchived(entry),
				entry.getOverpaid()
		);
	}

	private Entry mapToEntity(EntryDTO entryDTO, Entry entry, Bill bill, FlowType flowType) {
		entry.setId(entryDTO.getEntryId());
		entry.setBill(bill);
		entry.setDate(Date.valueOf(entryDTO.getDate()));
		entry.setAmount(entryDTO.getAmount());
		entry.setStatus(entryDTO.getStatus());
		entry.setServices(entryDTO.getServices());
		entry.setFlow(flowType.toString());
		if (entry.getOverpaid() == null) {
			entry.setOverpaid(false);
		}
		entry.setRecycleDate(entryDTO.getRecycle() ? LocalDateTime.now() : null);
		return entry;
	}
}
