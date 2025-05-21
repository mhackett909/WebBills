package com.projects.bills.DataHelpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntryFilters {
    private String userName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long invoiceNum;
    private List<String> partyList;
    private BigDecimal min;
    private BigDecimal max;
    private String flow;
    private Boolean paid;
    private Boolean overpaid;
    private Boolean archived;
}