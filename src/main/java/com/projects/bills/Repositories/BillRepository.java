package com.projects.bills.Repositories;

import com.projects.bills.Entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, String> {

}
