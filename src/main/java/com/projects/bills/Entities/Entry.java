package com.projects.bills.Entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
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

	@Column(name = "recycle_date")
	private LocalDateTime recycleDate;

	@Column(name = "invoiceID")
	private long invoiceId;

	@ManyToOne
	@JoinColumn(name = "userID", referencedColumnName = "id")
	private User user;

	@Column(name = "overpaid")
	private Boolean overpaid;

	@OneToMany(mappedBy="entry")
	@OrderBy("date DESC")
	private List<Payment> payments;

	@ManyToOne
	@JoinColumn(name="billID", referencedColumnName = "id")
	private Bill bill;

	@Column(name = "last_action")
	private String lastAction;

	@Formula("(SELECT e.amount - COALESCE(SUM(p.amount), 0) " +
			" FROM entry e " +
			" LEFT JOIN payment p ON p.entryID = e.id AND p.recycle_date IS NULL " +
			" WHERE e.id = id)")
	private BigDecimal balance;
}
