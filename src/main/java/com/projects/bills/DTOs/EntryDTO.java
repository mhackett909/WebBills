package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntryDTO {
	private long entryId;
	private long billId;
	private String name;
	private Date date;
	private BigDecimal amount;
	private Boolean status;
	private String services;
	private String flow;
	private Boolean archived;
}
