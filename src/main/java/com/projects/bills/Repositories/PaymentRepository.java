package com.projects.bills.Repositories;

import com.projects.bills.Entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByEntryIdAndRecycleDateIsNull(Long entryId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.entry.id = :entryId AND p.recycleDate IS NULL")
    BigDecimal sumAmountByEntryIdAndRecycleDateIsNull(@Param("entryId") Long entryId);
}