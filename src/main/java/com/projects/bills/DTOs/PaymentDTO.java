package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
	private long paymentId;
	private long entryId;
	private Date date;
	private BigDecimal amount;
	private String type;
	private String medium;
	private String notes;
}
