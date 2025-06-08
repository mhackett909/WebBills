package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.Constants.StatsResultKeys;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class StatsService {
    private final UserService userService;
    private final EntryRepository entryRepository;
    private final StatsHelper statsHelper;
    private final StatsMapper statsMapper;
    private final EntryMapper entryMapper;
    private final EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);

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
                logger.error("User not found: {}", userName);
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(Exceptions.USER_NOT_FOUND, userName)
                );
            }
            Entry entry = entryRepository.findByInvoiceIdAndUserAndRecycleDateIsNull(filters.getInvoiceNum(), user.get());
            if (entry == null) {
                logger.error(
                        "Invoice number {} not found for user {}",
                        filters.getInvoiceNum(),
                        user.get().getUsername()
                );
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(Exceptions.ENTRY_NOT_FOUND, filters.getInvoiceNum())
                );
            }
        }

        logger.debug("Building result map for stats with filters: {}", filters);
        Map<String, List<Object[]>> resultMap = buildResultMap(filters);

        logger.debug("Result map built with keys: {}", resultMap.keySet());
        return statsMapper.buildStatsDTO(resultMap);
    }

    private Map<String, List<Object[]>> buildResultMap(EntryFilters filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        logger.info("Calling statsHelper.getTotalEntryAmountsByFlow");
        CriteriaQuery<Object[]> query = statsHelper.getTotalEntryAmountsByFlow(cb, filters);

        Map<String, List<Object[]>> resultMap = new HashMap<>();

        resultMap.put(StatsResultKeys.TOTAL_ENTRY_AMOUNTS_BY_FLOW, entityManager.createQuery(query).getResultList());

        logger.info("Calling statsHelper.getMaxAvgSumQuery");
        query = statsHelper.getMaxAvgSumQuery(cb, filters);

        resultMap.put(StatsResultKeys.MAX_AVG_SUM, entityManager.createQuery(query).getResultList());

        logger.info("Calling statsHelper.getOverPaidEntryTotals");
        query = statsHelper.getOverpaidEntryTotals(cb, filters);

        resultMap.put(StatsResultKeys.OVERPAID_ENTRY_TOTALS, entityManager.createQuery(query).getResultList());

        logger.info("Calling statsHelper.getOverpaidPaymentTotals");
        query = statsHelper.getOverpaidPaymentTotals(cb, filters);

        resultMap.put(StatsResultKeys.OVERPAID_PAYMENT_TOTALS, entityManager.createQuery(query).getResultList());

        return buildTop5Totals(filters, resultMap);
    }

    private Map<String, List<Object[]>> buildTop5Totals(EntryFilters filters, Map<String, List<Object[]>> resultMap) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object[]> query;
        boolean switchBack = false;
        if (filters.getFlow() == null || filters.getFlow().equalsIgnoreCase(FlowType.OUTGOING.toString())) {
            if (filters.getFlow() == null) {
                switchBack = true;
                filters.setFlow(FlowType.OUTGOING.toString());
            }
            logger.info("Calling statsHelper.getTop5Parties for OUTGOING flow");
            query = statsHelper.getTop5Parties(cb, filters);

            resultMap.put(StatsResultKeys.TOP5_EXPENSE_RECEIPTS, entityManager.createQuery(query)
                    .setMaxResults(5)
                    .getResultList());

            logger.info("Calling statsHelper.getTop5TypeMediumCombos for OUTGOING flow");
            query = statsHelper.getTop5TypeMediumCombos(cb, filters);

            resultMap.put(StatsResultKeys.TOP5_EXPENSE_TYPE_MEDIUMS, entityManager.createQuery(query)
                    .setMaxResults(5)
                    .getResultList());
        } else {
            resultMap.put(StatsResultKeys.TOP5_EXPENSE_RECEIPTS, new ArrayList<>());
            resultMap.put(StatsResultKeys.TOP5_EXPENSE_TYPE_MEDIUMS, new ArrayList<>());
        }

        if (switchBack) {
            filters.setFlow(null);
        }

        if (filters.getFlow() == null || filters.getFlow().equalsIgnoreCase(FlowType.INCOMING.toString())) {
            if (filters.getFlow() == null) {
                filters.setFlow(FlowType.INCOMING.toString());
            }
            logger.info("Calling statsHelper.getTop5Parties for INCOMING flow");
            query = statsHelper.getTop5Parties(cb, filters);

            resultMap.put(StatsResultKeys.TOP5_INCOME_SOURCES, entityManager.createQuery(query)
                    .setMaxResults(5)
                    .getResultList());

            logger.info("Calling statsHelper.getTop5TypeMediumCombos for INCOMING flow");
            query = statsHelper.getTop5TypeMediumCombos(cb, filters);

            resultMap.put(StatsResultKeys.TOP5_INCOME_TYPE_MEDIUMS, entityManager.createQuery(query)
                    .setMaxResults(5)
                    .getResultList());
        } else {
            resultMap.put(StatsResultKeys.TOP5_INCOME_SOURCES, new ArrayList<>());
            resultMap.put(StatsResultKeys.TOP5_INCOME_TYPE_MEDIUMS, new ArrayList<>());
        }
        return resultMap;
    }
}
