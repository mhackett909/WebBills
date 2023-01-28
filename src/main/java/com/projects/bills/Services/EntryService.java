package com.projects.bills.Services;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Repositories.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntryService {
	private final EntryRepository entryRepository;
	
	@Autowired
	public EntryService(EntryRepository entryRepository) {
		this.entryRepository = entryRepository;
	}
	public List<Entry> getEntries() { return entryRepository.findAll(); }
	public void saveEntry(Entry entry) { entryRepository.save(entry); }
}
