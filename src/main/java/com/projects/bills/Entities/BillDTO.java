package com.projects.bills.Entities;

public class BillDTO {
		private long id;
		private String name;
		private Boolean status; //isArchived

		public String getName() { return name; }
	    public Boolean getStatus() { return status; }
	    public long getId() { return id; }

	    public void setName(String name) { this.name = name; }
		public void setStatus(Boolean status) { this.status = status; }
	    public void setId(long id) { this.id = id; }
}
