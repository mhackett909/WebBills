package com.projects.bills.Entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "entry")
public class Entry {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Date date;
	private BigDecimal amount;
	private Boolean status; // isPaid
	private String name; // obsolete, keeping for now
	private String services;
	private String flow;

	@OneToMany(mappedBy="entry")
	@OrderBy("date DESC")
	private List<Payment> payments;

	@ManyToOne
	@JoinColumn(name="billID", referencedColumnName = "id")
	private Bill bill;

	@Override
	public String toString() {
		return "Entry{" +
				"id=" + id +
				", date=" + date +
				", amount=" + amount +
				", status=" + status +
				", services='" + services + '\'' +
				", flow='" + flow + '\'' +
				'}';
	}
}
