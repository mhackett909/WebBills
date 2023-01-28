package com.projects.bills.Repositories;
import com.projects.bills.Entities.Entry;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Integer> {

}
