package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
	private long paymentId;
	private long entryId;
	private LocalDate date;
	private BigDecimal amount;
	private String type;
	private String medium;
	private String notes;
	private Boolean recycle = false;
}
