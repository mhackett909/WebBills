package com.projects.bills.Services;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.DataHelpers.StatsHelper;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Mappers.EntryMapper;
import com.projects.bills.Mappers.StatsMapper;
import com.projects.bills.Repositories.EntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntryService {
	private final EntryRepository entryRepository;
	private final BillService billService;
	private final UserService userService;
	private final EntityManager entityManager;
	private final StatsHelper statsHelper;
	private final EntryMapper entryMapper;
	private final StatsMapper statsMapper;

	@Autowired
	public EntryService(EntryRepository entryRepository,
                        BillService billService,
                        UserService userService,
                        EntityManager entityManager,
                        StatsHelper statsHelper,
                        EntryMapper entryMapper, StatsMapper statsMapper) {
		this.billService = billService;
		this.entryRepository = entryRepository;
        this.userService = userService;
        this.entityManager = entityManager;
        this.statsHelper = statsHelper;
        this.entryMapper = entryMapper;
        this.statsMapper = statsMapper;
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
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}

		EntryFilters filters = entryMapper.mapToEntryFilters(
				userName, startDate, endDate, invoiceNum, partyList,
				min, max, flow, paid, archives
		);

		Specification<Entry> spec = buildEntrySpecification(filters);

		if (sortField == null || sortField.isEmpty()) {
			sortField = "date";
		} else {
			sortField = entryMapper.mapSortField(sortField);
		}

		Sort.Direction sortDirection = Sort.Direction.DESC;
		if (sortOrder != null && sortOrder.equals("asc")) {
				sortDirection = Sort.Direction.ASC;
		}

		Sort sortBy = Sort.by(sortDirection, sortField);

		if (pageNum == null || pageNum < 0) {
			pageNum = 0;
		}

		if (pageSize == null || pageSize < 1) {
			pageSize = 25;
		}

		Pageable pageable = PageRequest.of(pageNum, pageSize, sortBy);

		Page<Entry> entryPages = entryRepository.findAll(spec, pageable);

		List<Entry> entries = entryPages.getContent();

		return mapEntriesToDTOList(entries, entryPages.getTotalElements());
	}

	public StatsDTO getStats(String userName,
							 LocalDate startDate,
							 LocalDate endDate,
							 Long invoiceNum,
							 List<String> partyList,
							 BigDecimal min,
							 BigDecimal max,
							 String flow,
							 String paid,
							 String archives) {

		EntryFilters filters = entryMapper.mapToEntryFilters(
				userName, startDate, endDate, invoiceNum, partyList,
				min, max, flow, paid, archives
		);

		if (filters.getInvoiceNum() != null) {
			Optional<User> user = userService.findByUsername(userName);
			if (user.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
			}
			Entry entry = entryRepository.findByIdAndRecycleDateIsNull(filters.getInvoiceNum());
			if (entry == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + filters.getInvoiceNum());
			}
			if (!entry.getBill().getUser().getUsername().equalsIgnoreCase(userName)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this entry");
			}
		}

		HashMap<String, List<Object[]>> resultMap = buildResultMap(filters);
		return statsMapper.buildStatsDTO(resultMap);
	}

	public Optional<EntryDTO> getEntryDtoById(Long id, String filter, String userName) {
		Optional<Entry> entry;
		// "bypass" filter allows access to recycled entries for restoration
		if (!"bypass".equalsIgnoreCase(filter)) {
			entry = Optional.ofNullable(entryRepository.findByIdAndRecycleDateIsNull(id));
		} else {
			entry = entryRepository.findById(id);
		}
		if (entry.isPresent()) {
			User entryUser = entry.get().getBill().getUser();
			if (!entryUser.getUsername().equalsIgnoreCase(userName)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this entry");
			}
		}
		return entry.map(e -> entryMapper.mapToDTO(e, isArchived(e)));
	}

	protected Optional<Entry> getEntryById(Long id) {
		return entryRepository.findById(id);
	}

	public EntryDTO saveEntry(EntryDTO entryDTO, boolean existing, String filter, String userName) {
		Entry entry;
		if (existing) {
			// Check if entry exists
			Optional<Entry> existingEntry = getEntryById(entryDTO.getEntryId());
			if (existingEntry.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found with id: " + entryDTO.getEntryId());
			}
			entry = existingEntry.get();

			if (!entry.getBill().getUser().getUsername().equalsIgnoreCase(userName)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this invoice");
			}

			// "bypass" filter allows access to recycled entries for restoration
			if (entry.getRecycleDate() != null && !"bypass".equalsIgnoreCase(filter)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a recycled entry");
			}

			if (!entry.getBill().getStatus()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update an entry linked to an archived entity");
			}
		} else {
			entry = new Entry();
		}

		Bill bill = billService.getBillEntityById(entryDTO.getBillId());
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill not found for id: " + entryDTO.getBillId());
		}

		User billUser = bill.getUser();

		if (!billUser.getUsername().equalsIgnoreCase(userName)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this bill");
		}

		FlowType type = FlowType.fromType(entryDTO.getFlow());

		long invoiceId = getInvoiceId(entryDTO, billUser);

		Entry mappedEntry = entryMapper.mapToEntity(entryDTO, entry, bill, type, billUser, invoiceId);

		Entry savedEntry = entryRepository.save(mappedEntry);
		return entryMapper.mapToDTO(savedEntry, isArchived(savedEntry));
	}

	private long getInvoiceId(EntryDTO entryDTO, User user) {
		long invoiceId;
		if (entryDTO.getInvoiceId() != 0) {
			invoiceId = entryDTO.getInvoiceId();
		} else {
			long newId = entryRepository.findNextInvoiceIdForUser(user);
			if (newId <= 0) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not retrieve next invoice ID");
			}
			invoiceId = newId;
		}
		return invoiceId;
	}

	private boolean isArchived(Entry entry) {
		Bill bill = billService.getBillEntityById(entry.getBill().getBillId());
		if (bill == null || bill.getStatus() == null) {
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

	private HashMap<String, List<Object[]>> buildResultMap(EntryFilters filters) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<Object[]> query = statsHelper.getTotalEntryAmountsByFlow(cb, filters);

		HashMap<String, List<Object[]>> resultMap = new HashMap<>();

		resultMap.put("totalEntryAmountsByFlow", entityManager.createQuery(query).getResultList());

		query = statsHelper.getmaxAvgSumQuery(cb, filters);

		resultMap.put("maxAvgSum", entityManager.createQuery(query).getResultList());

		query = statsHelper.getOverpaidEntryTotals(cb, filters);

		resultMap.put("overpaidEntryTotals", entityManager.createQuery(query).getResultList());

		query = statsHelper.getOverpaidPaymentTotals(cb, filters);

		resultMap.put("overpaidPaymentTotals", entityManager.createQuery(query).getResultList());

		return buildTop5Totals(filters, resultMap);
	}

	private HashMap<String, List<Object[]>> buildTop5Totals(EntryFilters filters, HashMap<String, List<Object[]>> resultMap) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<Object[]> query;
		boolean switchBack = false;
		if (filters.getFlow() == null || filters.getFlow().equalsIgnoreCase(FlowType.OUTGOING.toString())) {
			if (filters.getFlow() == null) {
				switchBack = true;
				filters.setFlow(FlowType.OUTGOING.toString());
			}
			query = statsHelper.getTop5Parties(cb, filters);

			resultMap.put("top5ExpenseReceipts", entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList());

			query = statsHelper.getTop5Types(cb, filters);

			resultMap.put("top5ExpenseTypes", entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList());
		} else {
			resultMap.put("top5ExpenseReceipts", new ArrayList<>());
			resultMap.put("top5ExpenseTypes", new ArrayList<>());
		}

		if (switchBack) {
			filters.setFlow(null);
		}

		if (filters.getFlow() == null || filters.getFlow().equalsIgnoreCase(FlowType.INCOMING.toString())) {
			if (filters.getFlow() == null) {
				filters.setFlow(FlowType.INCOMING.toString());
			}
			query = statsHelper.getTop5Parties(cb, filters);

			resultMap.put("top5IncomeSources", entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList());


			query = statsHelper.getTop5Types(cb, filters);

			resultMap.put("top5IncomeTypes", entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList());
		} else {
			resultMap.put("top5IncomeSources", new ArrayList<>());
			resultMap.put("top5IncomeTypes", new ArrayList<>());
		}
		return resultMap;
	}
}
