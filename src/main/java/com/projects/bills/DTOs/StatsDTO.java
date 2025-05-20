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
    private BigDecimal totalExpenseAmount;
    private BigDecimal totalExpenseUnpaid;
    private BigDecimal totalIncomeAmount;
    private BigDecimal totalIncomeOutstanding;
    private BigDecimal maxReceivedPaymentAmount;
    private BigDecimal maxSentPaymentAmount;
    private BigDecimal avgReceivedPaymentAmount;
    private BigDecimal avgSentPaymentAmount;
    private BigDecimal totalReceivedPaymentAmount;
    private BigDecimal totalSentPaymentAmount;
    private BigDecimal totalOverpaidReceived;
    private BigDecimal totalOverpaidSent;

    private Map<String, BigDecimal> topExpenseRecipients;
    private Map<String, BigDecimal> topIncomeSources;
    private Map<String, BigDecimal> topExpenseTypes;
    private Map<String, BigDecimal> topIncomeTypes;
}