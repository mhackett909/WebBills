package com.projects.bills.Entities;

import java.math.BigDecimal;
import java.sql.Date;

public class PaymentDTO {
	private long paymentId;
	private Date date;
	private BigDecimal amount;
	private String type;
	private String medium;
	private String notes;
	
	public long getPaymentId() { return paymentId; }
	public Date getDate() {	return date; }
	public BigDecimal getAmount() { return amount; }
	public String getType() { return type; }
	public String getMedium() { return medium; }
	public String getNotes() { return notes; }

	public void setPaymentId(long paymentId) { this.paymentId = paymentId; }
	public void setDate(Date date) { this.date = date; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public void setType(String type) { this.type = type; }
	public void setMedium(String medium) { this.medium = medium; }
	public void setNotes(String notes) { this.notes = notes; }
}
