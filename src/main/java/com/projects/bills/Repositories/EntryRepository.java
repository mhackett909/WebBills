package com.projects.bills.Repositories;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findAllByBillInAndRecycleDateIsNull(List<Bill> bills);
}
