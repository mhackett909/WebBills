package com.projects.bills.Entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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
	@Column(columnDefinition = "TINYINT")
	private Boolean autopay;

	@Column(name = "recycle_date")
	private LocalDateTime recycleDate;

	@Column(name = "last_action")
	private String lastAction;

	@ManyToOne
	@JoinColumn(name="entryID")
	private Entry entry;
}
