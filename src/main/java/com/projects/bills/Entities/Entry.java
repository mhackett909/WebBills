package com.projects.bills.Entities;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "entry")
public class Entry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long entryId;
	private Date date;
	private BigDecimal amount;
	private Boolean status; // isPaid
	private String services;

	@OneToMany(mappedBy="entry")
	@OrderBy("date DESC")
	private List<Payment> payments;

	@ManyToOne
	@JoinColumn(name="name", referencedColumnName = "name")
	private Bill bill;

	public Date getDate() { return date; }
	public Boolean getStatus() { return status; }
	public BigDecimal getAmount() {	return amount; }
	public String getServices() { return services; }
	public List<Payment> getPayments() { return payments; }
	public Bill getBill() { return bill; }
	public long getId() {
		return entryId;
	}

	public void setDate(Date date) { this.date = date; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public void setStatus(Boolean status) { this.status = status; }
	public void setServices(String services) { this.services = services; }
	public void setPayments(List<Payment> payments) { this.payments = payments; }
	public void setBill(Bill bill) { this.bill = bill; }

	@Override
	public String toString() {
		return "Entry{" + "id=" + entryId + ", date=" + date + ", amount=" + amount + ", status=" + status + ", services='" + services + '\'' + '}';
	}
}
