package com.projects.bills.Mappers;

import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.Enums.FlowType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatsMapper {
    public StatsDTO buildStatsDTO(Map<String, List<Object[]>> resultMap) {
        StatsDTO statsDTO = new StatsDTO();

        mapToStatsDTO(statsDTO, resultMap.get("totalEntryAmountsByFlow"), "totalEntryAmountsByFlow");

        mapToStatsDTO(statsDTO, resultMap.get("maxAvgSum"), "maxAvgSum");

        mapToStatsDTO(statsDTO, resultMap.get("top5ExpenseReceipts"), "top5ExpenseReceipts");

        mapToStatsDTO(statsDTO, resultMap.get("top5ExpenseTypes"), "top5ExpenseTypes");

        mapToStatsDTO(statsDTO, resultMap.get("top5IncomeSources"), "top5IncomeSources");

        mapToStatsDTO(statsDTO, resultMap.get("top5IncomeTypes"), "top5IncomeTypes");

        mapOverPaymentsToStatsDTO(
                statsDTO,
                resultMap.get("overpaidEntryTotals"),
                resultMap.get("overpaidPaymentTotals")
        );

        return statsDTO;
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
                Map<String, BigDecimal> top5ExpenseReceipts = new HashMap<>();
                for (Object[] result : resultList) {
                    String partyName = (String) result[0];
                    BigDecimal amount = (BigDecimal) result[2];
                    top5ExpenseReceipts.put(partyName, amount);
                }
                statsDTO.setTopExpenseRecipients(top5ExpenseReceipts);
                break;
            case "top5IncomeSources":
                Map<String, BigDecimal> top5IncomeSources = new HashMap<>();
                for (Object[] result : resultList) {
                    String partyName = (String) result[0];
                    BigDecimal amount = (BigDecimal) result[2];
                    top5IncomeSources.put(partyName, amount);
                }
                statsDTO.setTopIncomeSources(top5IncomeSources);
                break;
            case "top5ExpenseTypes":
                Map<String, BigDecimal> top5ExpenseTypes = new HashMap<>();
                for (Object[] result : resultList) {
                    String typeName = (String) result[1];
                    BigDecimal amount = (BigDecimal) result[2];
                    top5ExpenseTypes.put(typeName, amount);
                }
                statsDTO.setTopExpenseTypes(top5ExpenseTypes);
                break;
            case "top5IncomeTypes":
                Map<String, BigDecimal> top5IncomeTypes = new HashMap<>();
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
}
