package com.projects.bills.Entities;

public class BillDTO {
		private String name;
		private Boolean status; //isArchived
		public BillDTO(String name, boolean status) {
			this.name = name;
			this.status = status;
		}
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Boolean getStatus() { return status; }
		public void setStatus(Boolean status) { this.status = status; }
}
