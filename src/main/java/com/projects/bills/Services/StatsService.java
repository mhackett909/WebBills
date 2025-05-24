package com.projects.bills.Services;

import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.DataHelpers.StatsHelper;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Mappers.EntryMapper;
import com.projects.bills.Mappers.StatsMapper;
import com.projects.bills.Repositories.EntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
public class StatsService {
    private final UserService userService;
    private final EntryRepository entryRepository;
    private final StatsHelper statsHelper;
    private final StatsMapper statsMapper;
    private final EntryMapper entryMapper;
    private final EntityManager entityManager;

    public StatsService(UserService userService, EntryRepository entryRepository, StatsHelper statsHelper, StatsMapper statsMapper, EntryMapper entryMapper, EntityManager entityManager) {
        this.userService = userService;
        this.entryRepository = entryRepository;
        this.statsHelper = statsHelper;
        this.statsMapper = statsMapper;
        this.entryMapper = entryMapper;
        this.entityManager = entityManager;
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
