package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BalanceDTO {
    private BigDecimal totalBalance = BigDecimal.ZERO;
    private BigDecimal totalOverpaid = BigDecimal.ZERO;
}

