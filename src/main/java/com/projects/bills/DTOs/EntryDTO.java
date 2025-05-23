package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntryDTO {
	private long entryId; // Global ID
	private long billId;
	private long invoiceId; // Unique to users (local ID)
	private String name;
	private LocalDate date;
	private BigDecimal amount;
	private Boolean status;
	private Boolean recycle = false;
	private String services;
	private String flow;
	private Boolean archived;
	private Boolean overpaid;
}
