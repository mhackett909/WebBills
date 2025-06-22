package com.projects.bills.Services;
import com.projects.bills.Constants.Exceptions;
import com.projects.bills.Constants.Strings;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.DataHelpers.StatsHelper;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Mappers.EntryMapper;
import com.projects.bills.Repositories.EntryRepository;
import com.projects.bills.Repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EntryService {
	private final EntryRepository entryRepository;
	private final PaymentRepository paymentRepository;
	private final BillService billService;
	private final UserService userService;
	private final StatsHelper statsHelper;
	private final EntryMapper entryMapper;
	private static final Logger logger = LoggerFactory.getLogger(EntryService.class);

	@Autowired
	public EntryService(EntryRepository entryRepository, PaymentRepository paymentRepository,
                        BillService billService,
                        UserService userService,
                        StatsHelper statsHelper,
                        EntryMapper entryMapper) {
        this.paymentRepository = paymentRepository;
        this.billService = billService;
		this.entryRepository = entryRepository;
        this.userService = userService;
        this.statsHelper = statsHelper;
        this.entryMapper = entryMapper;
    }

	public EntryDTOList getEntries(String userName,
								   LocalDate startDate,
								   LocalDate endDate,
								   Long invoiceNum,
								   List<String> partyList,
								   BigDecimal min,
								   BigDecimal max,
								   String flow,
								   String paid,
								   String archives,
								   Integer pageNum,
								   Integer pageSize,
								   String sortField,
								   String sortOrder) {
		Optional<User> realUser = userService.findByUsername(userName);
		if (realUser.isEmpty()) {
			logger.error("User not found: {}", userName);
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					String.format(Exceptions.USER_NOT_FOUND, userName));
		}

		EntryFilters filters = entryMapper.mapToEntryFilters(
				userName, startDate, endDate, invoiceNum, partyList,
				min, max, flow, paid, archives
		);

		logger.debug("Fetching entries with filters: {}", filters);

		Specification<Entry> spec = buildEntrySpecification(filters);

		if (sortField == null || sortField.isEmpty()) {
			sortField = Strings.SORT_FIELD_DATE; // Default sort field
		} else {
			sortField = entryMapper.mapSortField(sortField);
		}

		Sort.Direction sortDirection = Sort.Direction.DESC;
		if (sortOrder != null && sortOrder.equals(Strings.SORT_DIR_ASC)) {
			sortDirection = Sort.Direction.ASC;
		}

		Sort sortBy;
		if (Strings.SORT_FIELD_DATE.equals(sortField)) {
			// If sorting by date, also sort by invoiceId to ensure consistent ordering
			sortBy = Sort.by(
					new Sort.Order(sortDirection, Strings.SORT_FIELD_DATE),
					new Sort.Order(sortDirection, Strings.SORT_FIELD_INVOICE_ID)
			);
		} else {
			sortBy = Sort.by(sortDirection, sortField);
		}

		if (pageNum == null || pageNum < 0) {
			pageNum = 0;
		}

		if (pageSize == null || pageSize < 1) {
			pageSize = 100;
		}

		Pageable pageable = PageRequest.of(pageNum, pageSize, sortBy);

		logger.debug("Fetching entries with pagination: pageNum={}, pageSize={}, sortField={}, sortOrder={}",
				pageNum, pageSize, sortField, sortOrder);

		Page<Entry> entryPages = entryRepository.findAll(spec, pageable);

		List<Entry> entries = entryPages.getContent();

		logger.debug("Found {} entries for user: {}", entries.size(), userName);
		return mapEntriesToDTOList(entries, entryPages.getTotalElements());
	}

	public Optional<EntryDTO> getEntryDtoById(Long id, String filter, String userName) {
		logger.debug("Fetching entry by ID: {}, filter: {}, user: {}", id, filter, userName);
		Optional<Entry> entry;
		// "bypass" filter allows access to recycled entries for restoration
		if (!Strings.EDIT_BYPASS.equalsIgnoreCase(filter)) {
			entry = Optional.ofNullable(entryRepository.findByIdAndRecycleDateIsNull(id));
		} else {
			entry = entryRepository.findById(id);
		}
		if (entry.isPresent()) {
			User entryUser = entry.get().getBill().getUser();
			if (!entryUser.getUsername().equalsIgnoreCase(userName)) {
				logger.error("User {} is not authorized to access entry with ID: {}", userName, id);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_ENTRY);
			}
			logger.info("Fetched entry with ID: {} for user: {}", id, userName);
		} else {
			logger.error("Entry with ID: {} not found for user: {}", id, userName);
		}

		return entry.map(e -> entryMapper.mapToDTO(e, isArchived(e)));
	}

	public EntryDTO saveEntry(EntryDTO entryDTO, boolean existing, String filter, String userName) {
		logger.debug("Saving entry: {}, existing: {}, filter: {}, user: {}", entryDTO, existing, filter, userName);
		Entry entry;
		if (existing) {
			// Check if entry exists
			Optional<Entry> existingEntry = getEntryById(entryDTO.getEntryId());
			if (existingEntry.isEmpty()) {
				logger.error("Entry not found with ID: {}", entryDTO.getEntryId());
				throw new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						String.format(Exceptions.ENTRY_NOT_FOUND, entryDTO.getEntryId())
				);
			}
			entry = existingEntry.get();

			if (!entry.getBill().getUser().getUsername().equalsIgnoreCase(userName)) {
				logger.error("User {} is not authorized to access entry with ID: {}", userName, entryDTO.getEntryId());
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_ENTRY);
			}

			// "bypass" filter allows access to recycled entries for restoration
			if (entry.getRecycleDate() != null && !Strings.EDIT_BYPASS.equalsIgnoreCase(filter)) {
				logger.error("Cannot update a recycled entry with ID: {}", entryDTO.getEntryId());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.CANNOT_UPDATE_RECYCLED_ENTRY);
			}

			if (!entry.getBill().getStatus() && !Strings.EDIT_BYPASS.equalsIgnoreCase(filter)) {
				logger.error("Cannot update an entry linked to an archived bill with ID: {}", entryDTO.getEntryId());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Exceptions.CANNOT_UPDATE_ENTRY_LINKED_ARCHIVED);
			}
		} else {
			entry = new Entry();
		}

		Bill bill = billService.getBillEntityById(entryDTO.getBillId());
		if (bill == null) {
			logger.error("Bill not found with ID: {}", entryDTO.getBillId());
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					String.format(Exceptions.BILL_NOT_FOUND,
					entryDTO.getBillId())
			);
		}

		User billUser = bill.getUser();

		if (!billUser.getUsername().equalsIgnoreCase(userName)) {
			logger.error("User {} is not authorized to access bill with ID: {}", userName, entryDTO.getBillId());
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, Exceptions.NOT_AUTHORIZED_TO_ACCESS_BILL);
		}

		FlowType type = FlowType.fromType(entryDTO.getFlow());

		long invoiceId = getInvoiceId(entryDTO, billUser);

		Entry mappedEntry = entryMapper.mapToEntity(entryDTO, entry, bill, type, billUser, invoiceId);

		Entry savedEntry = calculatePaid(mappedEntry);

		logger.info("Saved entry with ID: {}", savedEntry.getId());
		return entryMapper.mapToDTO(savedEntry, isArchived(savedEntry));
	}

	protected Entry calculatePaid(Entry entry) {
		logger.info("Calculating paid status for entryId={}", entry.getId());
		BigDecimal entryAmount = entry.getAmount();
		BigDecimal paidAmount = paymentRepository.sumAmountByEntryIdAndRecycleDateIsNull(entry.getId());

		// The entry is paid if the paid amount is greater than or equal to the entry amount
		entry.setStatus(entryAmount.compareTo(paidAmount) <= 0);

		// The entry is overpaid if the paid amount is greater than the entry amount
		entry.setOverpaid(paidAmount.compareTo(entryAmount) > 0);

		logger.debug("Saving entry: {}", entry);
		return entryRepository.save(entry);
	}

	protected Optional<Entry> getEntryById(Long id) {
		logger.debug("Fetching entry by ID: {}", id);
		return entryRepository.findById(id);
	}

	private long getInvoiceId(EntryDTO entryDTO, User user) {
		long invoiceId;
		if (entryDTO.getInvoiceId() != 0) {
			invoiceId = entryDTO.getInvoiceId();
		} else {
			long newId = entryRepository.findNextInvoiceIdForUser(user);
			if (newId <= 0) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, Exceptions.NEXT_INVOICE_ID_NOT_FOUND);
			}
			invoiceId = newId;
		}
		logger.debug("Determined invoice ID {} for user {}", invoiceId, user.getUsername());
		return invoiceId;
	}

	private boolean isArchived(Entry entry) {
		Bill bill = entry.getBill();
		if (bill == null) {
			return false;
		}
		// If bill's status is true, it is enabled (and therefore not archived)
		return !bill.getStatus();
	}

	private EntryDTOList mapEntriesToDTOList(List<Entry> entries, Long total) {
		ArrayList<EntryDTO> entryList = new ArrayList<>();
		for (Entry entry : entries) {
			EntryDTO entryDTO = entryMapper.mapToDTO(entry, isArchived(entry));
			entryList.add(entryDTO);
		}

		return entryMapper.mapEntriesToDTOList(entryList, total);
	}

	private Specification<Entry> buildEntrySpecification(EntryFilters filters) {
		return (root, query, criteriaBuilder)
				-> statsHelper.getFilteredPredicate(criteriaBuilder, filters, root);
	}
}

