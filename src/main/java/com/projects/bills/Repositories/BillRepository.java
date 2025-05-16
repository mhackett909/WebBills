package com.projects.bills.Repositories;

import com.projects.bills.Entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByName(String name);

    Optional<Bill> deleteByName(String name);
}
