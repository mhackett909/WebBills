package com.projects.bills.Entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "bill")
public class Bill {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long billId;
	private String name;
	private Boolean status; //isArchived

	@ManyToOne
	@JoinColumn(name="userID", referencedColumnName = "id")
	private User user;
	
	@OneToMany(mappedBy="bill")
	@OrderBy("date DESC")
	private List<Entry> entries;

	@Override
	public String toString() {
		return "Bill{" +
				"billId=" + billId +
				", name='" + name + '\'' +
				", status=" + status +
				'}';
	}
}
