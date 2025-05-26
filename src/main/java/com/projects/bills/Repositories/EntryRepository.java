package com.projects.bills.Repositories;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.User;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long>, JpaSpecificationExecutor<Entry> {
    Entry findByIdAndRecycleDateIsNull(long id);

    @Query("SELECT e FROM Entry e WHERE e.bill.user = :user AND e.recycleDate IS NOT NULL " +
            "AND e.bill NOT IN :bills")
    List<Entry> findAllByUserAndRecycleDateIsNotNullAndBillNotIn(
            @Param("user") User user,
            @Param("bills") List<Bill> bills
    );

    @Query("SELECT e FROM Entry e WHERE e.bill.user = :user AND e.recycleDate IS NOT NULL")
    List<Entry> findAllByUserAndRecycleDateIsNotNull(@Param("user") User user);

    @Query("SELECT COALESCE(MAX(e.invoiceId), 0) + 1 FROM Entry e WHERE e.user = :user")
    Long findNextInvoiceIdForUser(@Param("user") User user);

    @Nonnull
    @EntityGraph(attributePaths = "payments")
    Page<Entry> findAll(Specification<Entry> spec, Pageable pageable);
}
