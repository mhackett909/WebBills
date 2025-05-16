package com.projects.bills.Entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

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
	
	@ManyToOne
	@JoinColumn(name="entryID")
	private Entry entry;

	@Override
	public String toString() {
		return "Payment{" + "id=" + paymentId + ", entryID=" + entry.getId() + ", date=" + date + ", amount=" + amount + ", type='" + type + '\'' + ", medium='" + medium + '\'' + ", notes='" + notes + '\'' + '}';
	}
}
