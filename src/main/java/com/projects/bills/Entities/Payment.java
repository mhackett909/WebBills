package com.projects.bills.Entities;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "payment")
public class Payment {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long paymentId;
	private Date date;
	private BigDecimal amount;
	private String type;
	private String medium;
	private String notes;
	
	@ManyToOne
	@JoinColumn(name="entryID")
	private Entry entry;
	public Date getDate() { return date; }
	public BigDecimal getAmount() {	return amount; }
	public String getType() { return type; }
	public String getMedium() { return medium; }
	public String getNotes() { return notes; }
	public Entry getEntry() { return entry;	}
	public long getId() { return paymentId; }

	public void setDate(Date date) { this.date = date; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public void setType(String type) { this.type = type; }
	public void setMedium(String medium) { this.medium = medium; }
	public void setNotes(String notes) { this.notes = notes; }
	public void setEntry(Entry entry) {	this.entry = entry; }

	@Override
	public String toString() {
		return "Payment{" + "id=" + paymentId + ", entryID=" + entry.getId() + ", date=" + date + ", amount=" + amount + ", type='" + type + '\'' + ", medium='" + medium + '\'' + ", notes='" + notes + '\'' + '}';
	}
}
