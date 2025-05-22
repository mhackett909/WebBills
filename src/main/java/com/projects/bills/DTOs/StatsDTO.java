package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatsDTO {
    private BigDecimal totalExpenseAmount = BigDecimal.ZERO;
    private BigDecimal totalExpenseUnpaid = BigDecimal.ZERO;
    private BigDecimal totalExpenseOverpaid = BigDecimal.ZERO;
    private BigDecimal totalIncomeAmount = BigDecimal.ZERO;
    private BigDecimal totalIncomeOutstanding = BigDecimal.ZERO;
    private BigDecimal totalIncomeOverpaid = BigDecimal.ZERO;
    private BigDecimal maxReceivedPaymentAmount = BigDecimal.ZERO;
    private BigDecimal maxSentPaymentAmount = BigDecimal.ZERO;
    private BigDecimal avgReceivedPaymentAmount = BigDecimal.ZERO;
    private BigDecimal avgSentPaymentAmount = BigDecimal.ZERO;
    private BigDecimal totalReceivedPaymentAmount = BigDecimal.ZERO;
    private BigDecimal totalSentPaymentAmount = BigDecimal.ZERO;

    private Map<String, BigDecimal> topExpenseRecipients;
    private Map<String, BigDecimal> topIncomeSources;
    private Map<String, BigDecimal> topExpenseTypes;
    private Map<String, BigDecimal> topIncomeTypes;
}