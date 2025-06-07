package com.projects.bills.DataHelpers;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class StatsHelper {
    public CriteriaQuery<Object[]> getMaxAvgSumQuery(CriteriaBuilder cb,
                                                     EntryFilters filters) {
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Entry> entryRoot = query.from(Entry.class);
        Join<Entry, Payment> paymentJoin = entryRoot.join("payments", JoinType.INNER);

        Predicate predicate = getFilteredPredicate(cb, filters, entryRoot);
        predicate = cb.and(predicate, cb.isNull(paymentJoin.get("recycleDate")));

        // Selecting the stats we need
        query.multiselect(
                entryRoot.get("flow"),
                cb.max(paymentJoin.get("amount")),
                cb.avg(paymentJoin.get("amount")),
                cb.sum(paymentJoin.get("amount"))
        );

        query.where(predicate);
        query.groupBy(entryRoot.get("flow"));

        return query;
    }

    public CriteriaQuery<Object[]> getOverpaidEntryTotals(CriteriaBuilder cb, EntryFilters filters) {
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Entry> entryRoot = query.from(Entry.class);

        Predicate predicate = getFilteredPredicate(cb, filters, entryRoot);
        predicate = cb.and(predicate, cb.equal(entryRoot.get("overpaid"), true));

        query.multiselect(
                entryRoot.get("flow"),
                cb.sum(entryRoot.get("amount"))
        );

        query.where(predicate);
        query.groupBy(entryRoot.get("flow"));

        return query;
    }

    public CriteriaQuery<Object[]> getOverpaidPaymentTotals(CriteriaBuilder cb, EntryFilters filters) {
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Entry> entryRoot = query.from(Entry.class);
        Join<Entry, Payment> paymentJoin = entryRoot.join("payments", JoinType.INNER);

        Predicate predicate = getFilteredPredicate(cb, filters, entryRoot);
        predicate = cb.and(predicate, cb.equal(entryRoot.get("overpaid"), true));
        predicate = cb.and(predicate, cb.isNull(paymentJoin.get("recycleDate")));

        query.multiselect(
                entryRoot.get("flow"),
                cb.sum(paymentJoin.get("amount"))
        );

        query.where(predicate);
        query.groupBy(entryRoot.get("flow"));

        return query;
    }

    public CriteriaQuery<Object[]> getTotalEntryAmountsByFlow(CriteriaBuilder cb, EntryFilters filters) {
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Entry> entryRoot = query.from(Entry.class);

        Predicate predicate = getFilteredPredicate(cb, filters, entryRoot);

        query.multiselect(
                entryRoot.get("flow"),
                cb.sum(entryRoot.get("amount"))
        );

        query.where(predicate);
        query.groupBy(entryRoot.get("flow"));

        return query;
    }

    public CriteriaQuery<Object[]> getTop5Parties(CriteriaBuilder cb, EntryFilters filters) {
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Entry> entryRoot = query.from(Entry.class);
        Join<Entry, Payment> paymentJoin = entryRoot.join("payments", JoinType.INNER);
        Join<Entry, Bill> billJoin = entryRoot.join("bill", JoinType.INNER);

        Predicate predicate = getFilteredPredicate(cb, filters, entryRoot);
        predicate = cb.and(predicate, cb.isNull(paymentJoin.get("recycleDate")));

        // Select: bill name, flow, and sum of payment.amount
        query.multiselect(
                billJoin.get("name"),
                entryRoot.get("flow"),
                cb.sum(paymentJoin.get("amount"))
        );

        query.where(predicate);

        // GROUP BY bill name and flow
        query.groupBy(billJoin.get("name"), entryRoot.get("flow"));

        // ORDER BY total_paid DESC
        query.orderBy(cb.desc(cb.sum(paymentJoin.get("amount"))));

        return query;
    }

    public CriteriaQuery<Object[]> getTop5TypeMediumCombos(CriteriaBuilder cb, EntryFilters filters) {
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Entry> entryRoot = query.from(Entry.class);
        Join<Entry, Payment> paymentJoin = entryRoot.join("payments", JoinType.INNER);

        Predicate predicate = getFilteredPredicate(cb, filters, entryRoot);
        predicate = cb.and(predicate, cb.isNull(paymentJoin.get("recycleDate")));

        // Select: flow, payment_type, payment_medium, sum(amount)
        query.multiselect(
                entryRoot.get("flow"),
                paymentJoin.get("type"),
                paymentJoin.get("medium"),
                cb.sum(paymentJoin.get("amount"))
        );

        query.where(predicate);

        // GROUP BY e.flow, p.type, p.medium
        query.groupBy(
                entryRoot.get("flow"),
                paymentJoin.get("type"),
                paymentJoin.get("medium")
        );

        // ORDER BY total_amount DESC
        query.orderBy(cb.desc(cb.sum(paymentJoin.get("amount"))));

        return query;
    }

    public Predicate getFilteredPredicate(CriteriaBuilder cb,
                                           EntryFilters filters,
                                           Root<Entry> entryRoot) {
        Predicate predicate = cb.conjunction();

        // Must be only for requesting user
        predicate = cb.and(predicate, cb.equal(entryRoot.get("bill").get("user").get("username"), filters.getUserName()));

        // Exclude recycled entries
        predicate = cb.and(predicate, cb.isNull(entryRoot.get("recycleDate")));

        String flowType = filters.getFlow();
        if (flowType != null && !flowType.isEmpty()) {
            predicate = cb.and(predicate, cb.equal(entryRoot.get("flow"), flowType));
        }

        BigDecimal minAmount = filters.getMin();
        BigDecimal maxAmount = filters.getMax();
        if (minAmount != null && maxAmount != null) {
            predicate = cb.and(predicate, cb.between(entryRoot.get("amount"), minAmount, maxAmount));
        } else if (minAmount != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(entryRoot.get("amount"), minAmount));
        } else if (maxAmount != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(entryRoot.get("amount"), maxAmount));
        }

        LocalDate startDate = filters.getStartDate();
        LocalDate endDate = filters.getEndDate();
        if (startDate != null && endDate != null) {
            predicate = cb.and(predicate, cb.between(entryRoot.get("date"), startDate, endDate));
        } else if (startDate != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(entryRoot.get("date"), startDate));
        } else if (endDate != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(entryRoot.get("date"), endDate));
        }

        Long invoiceNum = filters.getInvoiceNum();
        if (invoiceNum != null) {
            predicate = cb.and(predicate, cb.equal(entryRoot.get("invoiceId"), invoiceNum));
        }

        List<String> partyList = filters.getPartyList();
        if (partyList != null && !partyList.isEmpty())        {
            predicate = cb.and(predicate, entryRoot.get("bill").get("name").in(partyList));
        }

        Boolean paid = filters.getPaid();
        if (paid != null) {
            predicate = cb.and(predicate, cb.equal(entryRoot.get("status"), paid));
        }

        Boolean overpaid = filters.getOverpaid();
        if (overpaid != null) {
            predicate = cb.and(predicate, cb.equal(entryRoot.get("overpaid"), overpaid));
        }

        Boolean partial = filters.getPartial();
        if (partial != null) {
            // Filter for partial payments: balance < amount
            // Note that isPaid must be false for this to work correctly (it is set in the mapper)
            predicate = cb.and(predicate, cb.lessThan(entryRoot.get("balance"), entryRoot.get("amount")));
        }

        Boolean enabled = filters.getArchived();
        if (enabled != null) {
            // Disabled = archived
            predicate = cb.and(predicate, cb.equal(entryRoot.get("bill").get("status"), !enabled));
        }

        return predicate;
    }
}
