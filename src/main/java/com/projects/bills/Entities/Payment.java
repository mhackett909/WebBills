package com.projects.bills.Entities;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
public class Payment {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer paymentId;
	private Date date;
	private BigDecimal amount;
	private String type;
	private String medium;
	private String notes;
	
	@ManyToOne
	@JoinColumn(name="entryID")
	private Entry entry;
	public Date getDate() { return date; }
	public void setDate(Date date) { this.date = date; }
	public BigDecimal getAmount() {	return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getMedium() { return medium; }
	public void setMedium(String medium) { this.medium = medium; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public Entry getEntry() { return entry;	}
	public void setEntry(Entry entry) {	this.entry = entry; }
	public Integer getId() { return paymentId; }
	
	@Override
	public String toString() {
		return "Payment{" + "id=" + paymentId + ", entryID=" + entry.getId() + ", date=" + date + ", amount=" + amount + ", type='" + type + '\'' + ", medium='" + medium + '\'' + ", notes='" + notes + '\'' + '}';
	}
}
