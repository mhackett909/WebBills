package com.projects.bills.Repositories;
import com.projects.bills.Entities.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Integer> {
	@Query(value = "select e.* " +
			"from entry e " +
			"join bill b on e.name = b.name " +
			"where e.date >= DATE_SUB(NOW(), INTERVAL 90 DAY) and b.status = 1 " +
			"order by e.date desc", nativeQuery = true)
	List<Entry> findEntryLast90Days();
	
}
