package com.projects.bills.DataHelpers;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Entities.User;
import com.projects.bills.Repositories.BillRepository;
import com.projects.bills.Repositories.EntryRepository;
import com.projects.bills.Repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StatsHelperIntegrationTest {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    private final StatsHelper statsHelper = new StatsHelper();

    @BeforeEach
    void setup() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("pw");
        user.setEmail("email");
        user.setCreatedAt(LocalDateTime.now());
        user.setMfaEnabled(false);
        user.setEnabled(true);
        user = userRepository.save(user);

        Bill bill = new Bill();
        bill.setName("TestBill");
        bill.setUser(user);
        bill.setStatus(true);
        bill = billRepository.save(bill);

        Entry entry1 = new Entry();
        entry1.setBill(bill);
        entry1.setUser(user);
        entry1.setAmount(BigDecimal.valueOf(100));
        entry1.setDate(Date.valueOf(LocalDate.now()));
        entry1.setFlow("INCOMING");
        entry1.setStatus(true);
        entry1.setInvoiceId(1);
        entryRepository.save(entry1);

        Entry entry2 = new Entry();
        entry2.setBill(bill);
        entry2.setUser(user);
        entry2.setAmount(BigDecimal.valueOf(200));
        entry2.setDate(Date.valueOf(LocalDate.now().minusDays(1)));
        entry2.setFlow("OUTGOING");
        entry2.setStatus(false);
        entry2.setInvoiceId(2);
        entryRepository.save(entry2);


        // Add payments for each entry
        Payment payment1 = new Payment();
        payment1.setEntry(entry1);
        payment1.setDate(Date.valueOf(LocalDate.now()));
        payment1.setAmount(BigDecimal.valueOf(100));
        payment1.setType("DEBIT");
        payment1.setMedium("Bank Transfer");
        // set other required fields...
        em.persist(payment1);

        Payment payment2 = new Payment();
        payment2.setEntry(entry2);
        payment2.setDate(Date.valueOf(LocalDate.now().minusDays(1)));
        payment2.setAmount(BigDecimal.valueOf(200));
        payment2.setType("CARD");
        payment2.setMedium("Online Payment");
        // set other required fields...
        em.persist(payment2);

        em.flush();
        em.clear();
    }

    @Test
    void testGetFilteredPredicate_byFlow() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setFlow("INCOMING");

        var cb = em.getCriteriaBuilder();
        var cq = em.getCriteriaBuilder().createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        TypedQuery<Entry> query = em.createQuery(cq);
        List<Entry> results = query.getResultList();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFlow()).isEqualTo("INCOMING");
    }

    @Test
    void testGetFilteredPredicate_byAmountRange() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setMin(BigDecimal.valueOf(150));
        filters.setMax(BigDecimal.valueOf(250));

        var cb = em.getCriteriaBuilder();
        var cq = em.getCriteriaBuilder().createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        TypedQuery<Entry> query = em.createQuery(cq);
        List<Entry> results = query.getResultList();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAmount()).isEqualByComparingTo("200");
    }

    @Test
    void testGetFilteredPredicate_byDateRange() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setStartDate(LocalDate.now().minusDays(2));
        filters.setEndDate(LocalDate.now());

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).hasSize(2);
    }

    @Test
    void testGetFilteredPredicate_byInvoiceNum() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setInvoiceNum(1L);

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getInvoiceId()).isEqualTo(1L);
    }

    @Test
    void testGetFilteredPredicate_byPartyList() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setPartyList(List.of("TestBill"));

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).hasSize(2);
    }

    @Test
    void testGetFilteredPredicate_byPaid() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setPaid(true);

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> Boolean.TRUE.equals(e.getStatus()));
    }

    @Test
    void testGetFilteredPredicate_byOverpaid() {
        // Set one entry as overpaid
        Entry entry = entryRepository.findAll().get(0);
        entry.setOverpaid(true);
        entryRepository.save(entry);

        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setOverpaid(true);

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> Boolean.TRUE.equals(e.getOverpaid()));
    }

    @Test
    void testGetFilteredPredicate_byArchived() {
        // Set bill as archived (status = false)
        Bill bill = billRepository.findAll().get(0);
        bill.setStatus(false);
        billRepository.save(bill);

        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setArchived(true);

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> Boolean.FALSE.equals(e.getBill().getStatus()));
    }

    @Test
    void testGetFilteredPredicate_startDateNull_endDateNow() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setEndDate(LocalDate.now());

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> !e.getDate().toLocalDate().isAfter(LocalDate.now()));
    }

    @Test
    void testGetFilteredPredicate_endDateNull_startDateSet() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setStartDate(LocalDate.now().minusDays(1));

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> !e.getDate().toLocalDate().isBefore(LocalDate.now().minusDays(1)));
    }

    @Test
    void testGetFilteredPredicate_minAmountNull_maxAmountSet() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setMax(BigDecimal.valueOf(150));

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> e.getAmount().compareTo(BigDecimal.valueOf(150)) <= 0);
    }

    @Test
    void testGetFilteredPredicate_maxAmountNull_minAmountSet() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setMin(BigDecimal.valueOf(150));

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> e.getAmount().compareTo(BigDecimal.valueOf(150)) >= 0);
    }

    @Test
    void testGetmaxAvgSumQuery() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getmaxAvgSumQuery(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        // Example: results.get(0)[0] = flow, [1] = max, [2] = avg, [3] = sum
        assertThat(results.get(0)[0]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[1]).isInstanceOf(BigDecimal.class);
    }

    @Test
    void testGetOverpaidEntryTotals() {
        // Mark one entry as overpaid
        Entry entry = entryRepository.findAll().get(0);
        entry.setOverpaid(true);
        entryRepository.save(entry);

        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getOverpaidEntryTotals(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0)[0]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[1]).isInstanceOf(BigDecimal.class);
    }

    @Test
    void testGetOverpaidPaymentTotals() {
        // Mark one entry as overpaid
        Entry entry = entryRepository.findAll().get(0);
        entry.setOverpaid(true);
        entryRepository.save(entry);

        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getOverpaidPaymentTotals(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0)[0]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[1]).isInstanceOf(BigDecimal.class);
    }

    @Test
    void testGetTotalEntryAmountsByFlow() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getTotalEntryAmountsByFlow(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0)[0]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[1]).isInstanceOf(BigDecimal.class);
    }

    @Test
    void testGetTop5Parties() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getTop5Parties(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0)[0]).isEqualTo("TestBill");
        assertThat(results.get(0)[1]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[2]).isInstanceOf(BigDecimal.class);
    }

    @Test
    void testGetTop5Types() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getTop5Types(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        assertThat(results.get(0)[0]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[1]).isIn("DEBIT", "CARD");
        assertThat(results.get(0)[2]).isInstanceOf(BigDecimal.class);
    }
}