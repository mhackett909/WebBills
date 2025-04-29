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
	private Integer entryId;
	private Date date;
	private BigDecimal amount;
	private Integer status;
	private String services;

	@OneToMany(mappedBy="entry")
	@OrderBy("date DESC")
	private List<Payment> payments;

	@ManyToOne
	@JoinColumn(name="name")
	private Bill bill;

	public Date getDate() { return date; }
	public void setDate(Date date) { this.date = date; }
	public BigDecimal getAmount() {	return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
	public String getServices() { return services; }
	public void setServices(String services) { this.services = services; }

	public List<Payment> getPayments() { return payments; }
	public void setPayments(List<Payment> payments) { this.payments = payments; }
	public Bill getBill() { return bill; }
	public void setBill(Bill bill) { this.bill = bill; }

	public Integer getId() {
		return entryId;
	}
	
	@Override
	public String toString() {
		return "Entry{" + "id=" + entryId + ", date=" + date + ", amount=" + amount + ", status=" + status + ", services='" + services + '\'' + '}';
	}
}
