package com.projects.bills.Services;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Repositories.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EntryService {
	private final EntryRepository entryRepository;
	private final BillService billService;

	@Autowired
	public EntryService(EntryRepository entryRepository, BillService billService) {
		this.billService = billService;
		this.entryRepository = entryRepository;
	}
	//In reality, will be getting entries by some parameters (id, date range, bill name, isDue, isOverpaid, isPaid, amount range)
	public List<EntryDTO> getEntries() {
		//List<Entry> entries = entryRepository.findAll();
		List<Entry> entries = entryRepository.findEntryLast90Days();
		ArrayList<EntryDTO> entryList = new ArrayList<>();
		for (Entry entry : entries) {
			EntryDTO entryDTO = new EntryDTO();
			entryDTO.setId(entry.getId());
			entryDTO.setName(entry.getBill().getName());
			entryDTO.setDate(entry.getDate());
			entryDTO.setAmount(entry.getAmount());
			entryDTO.setStatus(entry.getStatus());
			entryDTO.setServices(entry.getServices());
			entryDTO.setFlow(entry.getFlow());
			entryDTO.setArchived(isArchived(entry));
			entryList.add(entryDTO);
		}
		return entryList;
	}
	public void saveEntry(Entry entry) { entryRepository.save(entry); }

	private boolean isArchived(Entry entry) {
		BillDTO bill = billService.getBill(entry.getBill().getName());
		if (bill == null || bill.getStatus() == null) {
			return false;
		}
		// If bill's status is true, it is enabled (and therefore not archived)
		return !bill.getStatus();
	}
}
