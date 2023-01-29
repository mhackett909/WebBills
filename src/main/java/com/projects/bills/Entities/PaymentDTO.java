package com.projects.bills.Entities;

import java.math.BigDecimal;
import java.sql.Date;

public class PaymentDTO {
	private Integer paymentId;
	private Date date;
	private BigDecimal amount;
	private String type;
	private String medium;
	private String notes;
	
	public Integer getPaymentId() { return paymentId; }
	public void setPaymentId(Integer paymentId) { this.paymentId = paymentId; }
	public Date getDate() {	return date; }
	public void setDate(Date date) { this.date = date; }
	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getMedium() { return medium; }
	public void setMedium(String medium) { this.medium = medium; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
}
