package com.projects.bills.Entities;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "bill")
public class Bill {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long billId;
	private String name;
	private Boolean status; //isArchived
	
	@OneToMany(mappedBy="bill")
	@OrderBy("date DESC")
	private List<Entry> entries;

	public String getName() { return name; }
	public Boolean getStatus() { return status; }
	public long getBillId() { return billId; }
	public List<Entry> getEntries() { return entries; }

	public void setName(String name) { this.name = name; }
	public void setStatus(Boolean status) { this.status = status; }
	public void setEntries(List<Entry> entries) { this.entries = entries; }
	public void setBillId(long id) { this.billId = id; }

	@Override
	public String toString() {
		return "Bill{" +
				"billId=" + billId +
				", name='" + name + '\'' +
				", status=" + status +
				'}';
	}
}
