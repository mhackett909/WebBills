package com.projects.bills.Entities;

import java.math.BigDecimal;
import java.sql.Date;

public class EntryDTO {
	private long entryId;
	private String name;
	private Date date;
	private BigDecimal amount;
	private Boolean status;
	private String services;
	
	public String getName() { return name; }
	public long getId() { return entryId; }
	public Date getDate() { return date; }
	public BigDecimal getAmount() { return amount; }
	public Boolean getStatus() { return status; }
	public String getServices() { return services; }

	public void setName(String name) { this.name = name; }
	public void setId(long entryId) { this.entryId = entryId; }
	public void setDate(Date date) { this.date = date; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public void setStatus(Boolean status) { this.status = status; }
	public void setServices(String services) { this.services = services; }
}
