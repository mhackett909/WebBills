package com.projects.bills.Repositories;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findAllByUserAndRecycleDateIsNull(User user);
    List<Bill> findAllByStatusAndUserAndRecycleDateIsNull(Boolean status, User user);
}
