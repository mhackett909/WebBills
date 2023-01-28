package com.projects.bills.Entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.List;

@Entity
public class Bill {
	@Id
	private String name;
	private Boolean status; //isArchived
	
	@OneToMany(mappedBy="bill")
	@OrderBy("date DESC")
	private List<Entry> entries;
	
	public Bill() { }
	public Bill(String name, boolean status) {
		this.name = name;
		this.status = status;
	}
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Boolean getStatus() { return status; }
	public void setStatus(Boolean status) { this.status = status; }
	public List<Entry> getEntries() { return entries; }
	public void setEntries(List<Entry> entries) { this.entries = entries; }
	
	@Override
	public String toString() {
		return "Bill{" + "name='" + name + '\'' + ", status=" + status + '}';
	}
}
