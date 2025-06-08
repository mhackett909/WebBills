package com.projects.bills.Mappers;

import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.Enums.FlowType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StatsMapperTest {
    private final StatsMapper mapper;

    public StatsMapperTest() {
        mapper = new StatsMapper();
    }

    @Test
    void testBuildStatsDTO() {
        // Variables for totals
        BigDecimal totalExpenseAmount = new BigDecimal("1000.00");
        BigDecimal totalIncomeAmount = new BigDecimal("2000.00");
        BigDecimal maxSentPaymentAmount = new BigDecimal("500.00");
        BigDecimal avgSentPaymentAmount = new BigDecimal("250.00");
        BigDecimal totalSentPaymentAmount = new BigDecimal("800.00");
        BigDecimal maxReceivedPaymentAmount = new BigDecimal("700.00");
        BigDecimal avgReceivedPaymentAmount = new BigDecimal("350.00");
        BigDecimal totalReceivedPaymentAmount = new BigDecimal("1500.00");
        BigDecimal totalOverpaidExpenseExpected = new BigDecimal("100.00");
        BigDecimal totalOverpaidIncomeExpected = new BigDecimal("200.00");
        BigDecimal totalOverpaidExpenseActual = new BigDecimal("40.00");
        BigDecimal totalOverpaidIncomeActual = new BigDecimal("50.00");

        // Top recipients/types
        String expenseRecipient = "Vendor A";
        BigDecimal expenseRecipientAmount = new BigDecimal("300.00");
        String incomeSource = "Client B";
        BigDecimal incomeSourceAmount = new BigDecimal("400.00");
        String expenseType = "credit";
        String expenseMedium = "web";
        BigDecimal expenseTypeAmount = new BigDecimal("150.00");
        String incomeType = "cash";
        String incomeMedium = "person";
        BigDecimal incomeTypeAmount = new BigDecimal("250.00");

        // Prepare resultMap
        Map<String, List<Object[]>> resultMap = new HashMap<>();

        // totalEntryAmountsByFlow
        List<Object[]> totalEntryAmountsByFlow = Arrays.asList(
                new Object[]{FlowType.OUTGOING.toString(), totalExpenseAmount},
                new Object[]{FlowType.INCOMING.toString(), totalIncomeAmount}
        );
        resultMap.put("totalEntryAmountsByFlow", totalEntryAmountsByFlow);

        // maxAvgSum
        List<Object[]> maxAvgSum = Arrays.asList(
                new Object[]{FlowType.OUTGOING.toString(), maxSentPaymentAmount, avgSentPaymentAmount.doubleValue(), totalSentPaymentAmount},
                new Object[]{FlowType.INCOMING.toString(), maxReceivedPaymentAmount, avgReceivedPaymentAmount.doubleValue(), totalReceivedPaymentAmount}
        );
        resultMap.put("maxAvgSum", maxAvgSum);

        // top5ExpenseReceipts
        List<Object[]> top5ExpenseReceipts = Collections.singletonList(
                new Object[]{expenseRecipient, FlowType.OUTGOING.toString(), expenseRecipientAmount}
        );
        resultMap.put("top5ExpenseReceipts", top5ExpenseReceipts);

        // top5IncomeSources
        List<Object[]> top5IncomeSources = Collections.singletonList(
                new Object[]{incomeSource, FlowType.INCOMING.toString(), incomeSourceAmount}
        );
        resultMap.put("top5IncomeSources", top5IncomeSources);

        // top5ExpenseTypeMediumCombos
        List<Object[]> top5ExpenseTypeMediumCombos = Collections.singletonList(
                new Object[]{FlowType.OUTGOING.toString(), expenseType, expenseMedium, expenseTypeAmount}
        );
        resultMap.put("top5ExpenseTypeMediumCombos", top5ExpenseTypeMediumCombos);

        // top5IncomeTypeMediumCombos
        List<Object[]> top5IncomeTypeMediumCombos = Collections.singletonList(
                new Object[]{FlowType.INCOMING.toString(), incomeType, incomeMedium, incomeTypeAmount}
        );
        resultMap.put("top5IncomeTypeMediumCombos", top5IncomeTypeMediumCombos);

        // overpaidEntryTotals
        List<Object[]> overpaidEntryTotals = Arrays.asList(
                new Object[]{FlowType.OUTGOING.toString(), totalOverpaidExpenseExpected},
                new Object[]{FlowType.INCOMING.toString(), totalOverpaidIncomeExpected}
        );
        resultMap.put("overpaidEntryTotals", overpaidEntryTotals);

        // overpaidPaymentTotals
        List<Object[]> overpaidPaymentTotals = Arrays.asList(
                new Object[]{FlowType.OUTGOING.toString(), totalOverpaidExpenseActual},
                new Object[]{FlowType.INCOMING.toString(), totalOverpaidIncomeActual}
        );
        resultMap.put("overpaidPaymentTotals", overpaidPaymentTotals);

        // Act
        StatsDTO dto = mapper.buildStatsDTO(resultMap);

        // Assert
        assertNotNull(dto);
        assertEquals(totalExpenseAmount, dto.getTotalExpenseAmount());
        assertEquals(totalIncomeAmount, dto.getTotalIncomeAmount());
        assertEquals(maxSentPaymentAmount, dto.getMaxSentPaymentAmount());
        assertEquals(0, avgSentPaymentAmount.compareTo(dto.getAvgSentPaymentAmount()));
        assertEquals(maxReceivedPaymentAmount, dto.getMaxReceivedPaymentAmount());
        assertEquals(0, avgReceivedPaymentAmount.compareTo(dto.getAvgReceivedPaymentAmount()));

        // Overpaid calculations
        BigDecimal expectedExpenseOverpaid = totalOverpaidExpenseExpected.subtract(totalOverpaidExpenseActual).abs();
        BigDecimal expectedIncomeOverpaid = totalOverpaidIncomeExpected.subtract(totalOverpaidIncomeActual).abs();
        assertEquals(expectedExpenseOverpaid, dto.getTotalExpenseOverpaid());
        assertEquals(expectedIncomeOverpaid, dto.getTotalIncomeOverpaid());

        // Adjusted sent/received payment amounts
        assertEquals(totalSentPaymentAmount.subtract(expectedExpenseOverpaid), dto.getTotalSentPaymentAmount());
        assertEquals(totalReceivedPaymentAmount.subtract(expectedIncomeOverpaid), dto.getTotalReceivedPaymentAmount());

        // Unpaid/outstanding
        assertEquals(totalExpenseAmount.subtract(dto.getTotalSentPaymentAmount()), dto.getTotalExpenseUnpaid());
        assertEquals(totalIncomeAmount.subtract(dto.getTotalReceivedPaymentAmount()), dto.getTotalIncomeOutstanding());

        // Top recipients/types
        assertEquals(expenseRecipientAmount, dto.getTopExpenseRecipients().get(expenseRecipient));
        assertEquals(incomeSourceAmount, dto.getTopIncomeSources().get(incomeSource));
        assertEquals(expenseTypeAmount, dto.getTopExpenseTypes().get(expenseType+"|"+expenseMedium));
        assertEquals(incomeTypeAmount, dto.getTopIncomeTypes().get(incomeType+"|"+incomeMedium));
    }
}