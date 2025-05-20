package com.projects.bills.Repositories.Specifications;

import com.projects.bills.Entities.Entry;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class EntrySpecifications {
    public static Specification<Entry> hasFlow(String flow) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("flow"), flow);
    }

    public static Specification<Entry> hasBillNames(List<String> billNames) {
        return (root, query, criteriaBuilder) ->
                root.get("bill").get("name").in(billNames);
    }

    public static Specification<Entry> hasDateRange(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("date"), startDate, endDate);
    }

    public static Specification<Entry> hasAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
    }

    public static Specification<Entry> hasInvoiceNumber(Integer invoiceNumber) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), invoiceNumber);
    }

    public static Specification<Entry> isPaid(Boolean paid) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), paid);
    }

    public static Specification<Entry> isOverPaid(Boolean overPaid) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("overpaid"), overPaid);
    }

    public static Specification<Entry> isArchived(Boolean enabled) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("bill").get("status"), !enabled);
    }

    public static Specification<Entry> hasUserName(String userName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("bill").get("user").get("username"), userName);
    }
}
