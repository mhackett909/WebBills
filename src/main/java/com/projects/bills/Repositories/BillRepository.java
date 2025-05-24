package com.projects.bills.Repositories;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByUserAndRecycleDateIsNullOrderByNameAsc(User user);
    List<Bill> findAllByUserAndRecycleDateIsNotNullOrderByNameAsc(User user);
    List<Bill> findAllByStatusAndUserAndRecycleDateIsNullOrderByNameAsc(Boolean status, User user);
}
