package com.projects.bills.Services;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.DataHelpers.StatsHelper;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.sql.Date;

@Service
@Transactional
public class EntryService {
	private final EntryRepository entryRepository;
	private final BillService billService;
	private final UserService userService;
	private final EntityManager entityManager;
	private final StatsHelper statsHelper;

	@Autowired
	public EntryService(EntryRepository entryRepository, BillService billService, UserService userService, EntityManager entityManager, StatsHelper statsHelper) {
		this.billService = billService;
		this.entryRepository = entryRepository;
        this.userService = userService;
        this.entityManager = entityManager;
        this.statsHelper = statsHelper;
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

		EntryFilters filters = mapToEntryFilters(
				userName, startDate, endDate, invoiceNum, partyList,
				min, max, flow, paid, archives
		);

		Specification<Entry> spec = buildEntrySpecification(filters);

		if (sortField == null || sortField.isEmpty()) {
			sortField = "date";
		} else {
			sortField = mapSortField(sortField);
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
			pageSize = 100;
		}

		Pageable pageable = PageRequest.of(pageNum, pageSize, sortBy);

		Page<Entry> entryPages = entryRepository.findAll(spec, pageable);

		List<Entry> entries = entryPages.getContent();

		ArrayList<EntryDTO> entryList = new ArrayList<>();
		for (Entry entry : entries) {
			EntryDTO entryDTO = mapToDTO(entry);
			entryList.add(entryDTO);
		}

		EntryDTOList entryDtoList = new EntryDTOList();
		entryDtoList.setEntries(entryList);
		entryDtoList.setTotal(entryPages.getTotalElements());

		return entryDtoList;
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

		EntryFilters filters = mapToEntryFilters(
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

		StatsDTO statsDTO = new StatsDTO();

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<Object[]> query = statsHelper.getTotalEntryAmountsByFlow(cb, filters);

		List<Object[]> totalEntryAmountsByFlow = entityManager.createQuery(query).getResultList();

		mapToStatsDTO(statsDTO, totalEntryAmountsByFlow, "totalEntryAmountsByFlow");

		query = statsHelper.getmaxAvgSumQuery(cb, filters);

		List<Object[]> maxAvgSumResults = entityManager.createQuery(query).getResultList();

		mapToStatsDTO(statsDTO, maxAvgSumResults, "maxAvgSum");

		query = statsHelper.getOverpaidEntryTotals(cb, filters);

		List<Object[]> overpaidEntryTotals = entityManager.createQuery(query).getResultList();

		query = statsHelper.getOverpaidPaymentTotals(cb, filters);

		List<Object[]> overpaidPaymentTotals = entityManager.createQuery(query).getResultList();

		mapOverPaymentsToStatsDTO(statsDTO, overpaidEntryTotals, overpaidPaymentTotals);

		boolean switchBack = false;
		if (filters.getFlow() == null || filters.getFlow().equalsIgnoreCase(FlowType.OUTGOING.toString())) {
			if (filters.getFlow() == null) {
				switchBack = true;
				filters.setFlow(FlowType.OUTGOING.toString());
			}
			query = statsHelper.getTop5Parties(cb, filters);

			List<Object[]> top5ExpenseReceipts = entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList();

			mapToStatsDTO(statsDTO, top5ExpenseReceipts, "top5ExpenseReceipts");

			query = statsHelper.getTop5Types(cb, filters);

			List<Object[]> top5ExpenseTypes = entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList();

			mapToStatsDTO(statsDTO, top5ExpenseTypes, "top5ExpenseTypes");
		}

		if (switchBack) {
			filters.setFlow(null);
		}

		if (filters.getFlow() == null || filters.getFlow().equalsIgnoreCase(FlowType.INCOMING.toString())) {
			if (filters.getFlow() == null) {
				filters.setFlow(FlowType.INCOMING.toString());
			}
			query = statsHelper.getTop5Parties(cb, filters);

			List<Object[]> top5IncomeSources = entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList();

			mapToStatsDTO(statsDTO, top5IncomeSources, "top5IncomeSources");

			query = statsHelper.getTop5Types(cb, filters);

			List<Object[]> top5IncomeTypes = entityManager.createQuery(query)
					.setMaxResults(5)
					.getResultList();

			mapToStatsDTO(statsDTO, top5IncomeTypes, "top5IncomeTypes");
		}

		return statsDTO;
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
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update an entry linked to an archived entity");
			}
		} else {
			entry = new Entry();
		}

		Bill bill = billService.getBillEntityById(entryDTO.getBillId());
		if (bill == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill not found for id: " + entryDTO.getBillId());
		}

		User user = bill.getUser();

		String requestingUser = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!user.getUsername().equalsIgnoreCase(requestingUser)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this bill");
		}

		FlowType type = FlowType.fromType(entryDTO.getFlow());

		Entry mappedEntry = mapToEntity(entryDTO, entry, bill, type, user);

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
				entry.getInvoiceId(), // Local invoice ID
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

	private Entry mapToEntity(EntryDTO entryDTO, Entry entry, Bill bill, FlowType flowType, User user) {
		entry.setId(entryDTO.getEntryId());
		entry.setBill(bill);
		entry.setUser(user);
		entry.setDate(Date.valueOf(entryDTO.getDate()));
		entry.setAmount(entryDTO.getAmount());
		entry.setStatus(entryDTO.getStatus());
		entry.setServices(entryDTO.getServices());
		entry.setFlow(flowType.toString());
		entry.setOverpaid(entryDTO.getOverpaid());
		if (entry.getOverpaid() == null) {
			entry.setOverpaid(false);
		}
		if (entryDTO.getInvoiceId() != 0) {
			entry.setInvoiceId(entryDTO.getInvoiceId());
		} else {
			long newId = entryRepository.findNextInvoiceIdForUser(entry.getUser());
			if (newId <= 0) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not retrieve next invoice ID");
			}
			entry.setInvoiceId(newId);
		}
		entry.setRecycleDate(entryDTO.getRecycle() ? LocalDateTime.now() : null);
		return entry;
	}

	private EntryFilters mapToEntryFilters(String userName,
										 LocalDate startDate,
										 LocalDate endDate,
										 Long invoiceNum,
										 List<String> partyList,
										 BigDecimal min,
										 BigDecimal max,
										 String flow,
										 String paid,
										 String archives) {
		String flowType = null;
		if (flow != null && !flow.isEmpty()) {
			flowType = FlowType.fromType(flow).toString();
		}

		Boolean isPaid = null;
		Boolean isOverpaid = null;
		if (paid != null) {
			if (paid.equalsIgnoreCase("true")) {
				isPaid = true;
			} else if (paid.equalsIgnoreCase("false")) {
				isPaid = false;
			} else if (paid.equalsIgnoreCase("overpaid")) {
				isOverpaid = true;
			}
		}

		Boolean isArchived = null;
		if (archives != null) {
			if (archives.equalsIgnoreCase("true")) {
				isArchived = true;
			} else if (archives.equalsIgnoreCase("false")) {
				isArchived = false;
			}
		}

		EntryFilters filters = new EntryFilters();
		filters.setUserName(userName);
		filters.setStartDate(startDate);
		filters.setEndDate(endDate);
		filters.setInvoiceNum(invoiceNum);
		filters.setPartyList(partyList);
		filters.setMin(min);
		filters.setMax(max);
		filters.setFlow(flowType);
		filters.setPaid(isPaid);
		filters.setOverpaid(isOverpaid);
		filters.setArchived(isArchived);
		return filters;
	}

	private void mapToStatsDTO(StatsDTO statsDTO, List<Object[]> resultList, String resultType) {
		switch (resultType) {
			case "totalEntryAmountsByFlow":
				for (Object[] result : resultList) {
					String flowType = (String) result[0];
					if (flowType.equals(FlowType.OUTGOING.toString())) {
						statsDTO.setTotalExpenseAmount((BigDecimal) result[1]);
					} else if (flowType.equals(FlowType.INCOMING.toString())) {
						statsDTO.setTotalIncomeAmount((BigDecimal) result[1]);
					}
				}
				break;
			case "maxAvgSum":
				for (Object[] result : resultList) {
					String flowType = (String) result[0];
					if (flowType.equals(FlowType.OUTGOING.toString())) {
						statsDTO.setMaxSentPaymentAmount((BigDecimal) result[1]);
						statsDTO.setAvgSentPaymentAmount(BigDecimal.valueOf((Double) result[2]));
						statsDTO.setTotalSentPaymentAmount((BigDecimal) result[3]);
					} else if (flowType.equals(FlowType.INCOMING.toString())) {
						statsDTO.setMaxReceivedPaymentAmount((BigDecimal) result[1]);
						statsDTO.setAvgReceivedPaymentAmount(BigDecimal.valueOf((Double) result[2]));
						statsDTO.setTotalReceivedPaymentAmount((BigDecimal) result[3]);
					}
				}
				break;
				case "top5ExpenseReceipts":
					HashMap<String, BigDecimal> top5ExpenseReceipts = new HashMap<>();
					for (Object[] result : resultList) {
						String partyName = (String) result[0];
						BigDecimal amount = (BigDecimal) result[2];
						top5ExpenseReceipts.put(partyName, amount);
					}
					statsDTO.setTopExpenseRecipients(top5ExpenseReceipts);
				break;
				case "top5IncomeSources":
					HashMap<String, BigDecimal> top5IncomeSources = new HashMap<>();
					for (Object[] result : resultList) {
						String partyName = (String) result[0];
						BigDecimal amount = (BigDecimal) result[2];
						top5IncomeSources.put(partyName, amount);
					}
					statsDTO.setTopIncomeSources(top5IncomeSources);
				break;
				case "top5ExpenseTypes":
					HashMap<String, BigDecimal> top5ExpenseTypes = new HashMap<>();
					for (Object[] result : resultList) {
						String typeName = (String) result[1];
						BigDecimal amount = (BigDecimal) result[2];
						top5ExpenseTypes.put(typeName, amount);
					}
					statsDTO.setTopExpenseTypes(top5ExpenseTypes);
					break;
				case "top5IncomeTypes":
					HashMap<String, BigDecimal> top5IncomeTypes = new HashMap<>();
					for (Object[] result : resultList) {
						String typeName = (String) result[1];
						BigDecimal amount = (BigDecimal) result[2];
						top5IncomeTypes.put(typeName, amount);
					}
					statsDTO.setTopIncomeTypes(top5IncomeTypes);
				break;
		}
	}
	private void mapOverPaymentsToStatsDTO(StatsDTO statsDTO, List<Object[]> overpaidEntryTotals, List<Object[]> overpaidPaymentTotals) {
		BigDecimal totalOverpaidExpenseExpected = BigDecimal.ZERO;
		BigDecimal totalOverpaidIncomeExpected = BigDecimal.ZERO;
		for (Object[] result : overpaidEntryTotals) {
			String flowType = (String) result[0];
			if (flowType.equals(FlowType.OUTGOING.toString())) {
				totalOverpaidExpenseExpected = (BigDecimal) result[1];
			} else if (flowType.equals(FlowType.INCOMING.toString())) {
				totalOverpaidIncomeExpected = (BigDecimal) result[1];
			}
		}

		BigDecimal totalOverpaidExpenseActual = BigDecimal.ZERO;
		BigDecimal totalOverpaidIncomeActual = BigDecimal.ZERO;
		for (Object[] result : overpaidPaymentTotals) {
			String flowType = (String) result[0];
			if (flowType.equals(FlowType.OUTGOING.toString())) {
				totalOverpaidExpenseActual = (BigDecimal) result[1];
			} else if (flowType.equals(FlowType.INCOMING.toString())) {
				totalOverpaidIncomeActual = (BigDecimal) result[1];
			}
		}

		adjustOverpaidAmounts(statsDTO, totalOverpaidExpenseExpected, totalOverpaidExpenseActual, totalOverpaidIncomeExpected, totalOverpaidIncomeActual);
		deriveRemainingColumns(statsDTO);
	}

	private void adjustOverpaidAmounts(StatsDTO statsDTO,
									   BigDecimal totalOverpaidExpenseExpected,
									   BigDecimal totalOverpaidExpenseActual,
									   BigDecimal totalOverpaidIncomeExpected,
									   BigDecimal totalOverpaidIncomeActual) {
		BigDecimal totalOverpaidExpense = totalOverpaidExpenseExpected.subtract(totalOverpaidExpenseActual).abs();
		BigDecimal totalOverpaidIncome = totalOverpaidIncomeExpected.subtract(totalOverpaidIncomeActual).abs();

		statsDTO.setTotalExpenseOverpaid(totalOverpaidExpense);
		statsDTO.setTotalIncomeOverpaid(totalOverpaidIncome);

		BigDecimal adjustedTotalSentPaymentAmount = statsDTO.getTotalSentPaymentAmount().subtract(totalOverpaidExpense);
		statsDTO.setTotalSentPaymentAmount(adjustedTotalSentPaymentAmount);

		BigDecimal adjustedTotalReceivedPaymentAmount = statsDTO.getTotalReceivedPaymentAmount().subtract(totalOverpaidIncome);
		statsDTO.setTotalReceivedPaymentAmount(adjustedTotalReceivedPaymentAmount);
	}

	private void deriveRemainingColumns(StatsDTO statsDTO) {
		BigDecimal totalExpenseUnpaid = statsDTO.getTotalExpenseAmount()
				.subtract(statsDTO.getTotalSentPaymentAmount());

		BigDecimal totalIncomeOutstanding = statsDTO.getTotalIncomeAmount()
				.subtract(statsDTO.getTotalReceivedPaymentAmount());

		statsDTO.setTotalExpenseUnpaid(totalExpenseUnpaid);
		statsDTO.setTotalIncomeOutstanding(totalIncomeOutstanding);
	}

	private Specification<Entry> buildEntrySpecification(EntryFilters filters) {
		return (root, query, criteriaBuilder)
				-> statsHelper.getFilteredPredicate(criteriaBuilder, filters, root);
	}

	private String mapSortField(String sortField) {
		switch (sortField) {
			case "paid":
				return "status";
			case "name":
				return "bill.name";
			case "entryId":
				return "id";
			case "description":
				return "services";
			case "archived":
				return "bill.status";
			default:
				return sortField;
		}
	}
}
