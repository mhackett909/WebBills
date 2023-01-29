package com.projects.bills.Entities;

import java.math.BigDecimal;
import java.sql.Date;

public class EntryDTO {
	private Integer entryId;
	private String name;
	private Date date;
	private BigDecimal amount;
	private Integer status;
	private String services;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Integer getId() { return entryId; }
	public void setId(Integer entryId) { this.entryId = entryId; }
	public Date getDate() { return date; }
	public void setDate(Date date) { this.date = date; }
	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
	public String getServices() { return services; }
	public void setServices(String services) { this.services = services; }
}
