package com.projects.bills.Entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean status; //isArchived

	@Column(name = "recycle_date")
	private LocalDateTime recycleDate;

	@ManyToOne
	@JoinColumn(name="userID", referencedColumnName = "id")
	private User user;
	
	@OneToMany(mappedBy="bill")
	@OrderBy("date DESC")
	private List<Entry> entries;
}
