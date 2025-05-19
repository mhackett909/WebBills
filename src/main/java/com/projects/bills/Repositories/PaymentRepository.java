package com.projects.bills.Repositories;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByEntryIdAndRecycleDateIsNull(Long entryId);

    @Query("SELECT p FROM Payment p WHERE p.entry.bill.user = :user AND p.recycleDate IS NOT NULL " +
            "AND p.entry NOT IN :entries")
    List<Payment> findAllByUserAndRecycleDateIsNotNullAndEntryNotIn(
            @Param("user") User user,
            @Param("entries") List<Entry> entries
    );

    @Query("SELECT p FROM Payment p WHERE p.entry.bill.user = :user AND p.recycleDate IS NOT NULL " +
            "AND p.entry.bill NOT IN :bills" )
    List<Payment> findAllByUserAndRecycleDateIsNotNullAndBillNotIn(
            @Param("user") User user,
            @Param("bills") List<Bill> bills
    );

    @Query("SELECT p FROM Payment p WHERE p.entry.bill.user = :user AND p.recycleDate IS NOT NULL " +
            "AND p.entry.bill NOT IN :bills " +
            "AND p.entry NOT IN :entries")
    List<Payment> findAllByUserAndRecycleDateIsNotNullAndEntryNotInAndBillNotIn(
            @Param("user") User user,
            @Param("entries") List<Entry> entries,
            @Param("bills") List<Bill> bills
    );

    @Query("SELECT p FROM Payment p WHERE p.entry.bill.user = :user AND p.recycleDate IS NOT NULL")
    List<Payment> findAllByUserAndRecycleDateIsNotNull(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.entry.id = :entryId AND p.recycleDate IS NULL")
    BigDecimal sumAmountByEntryIdAndRecycleDateIsNull(@Param("entryId") Long entryId);
}