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
        entry1.setInvoiceId(1);
        entry1.setStatus(true);
        entry1.setOverpaid(false);
        entryRepository.save(entry1);

        Entry entry2 = new Entry();
        entry2.setBill(bill);
        entry2.setUser(user);
        entry2.setAmount(BigDecimal.valueOf(200));
        entry2.setDate(Date.valueOf(LocalDate.now().minusDays(1)));
        entry2.setFlow("OUTGOING");
        entry2.setStatus(false);
        entry2.setOverpaid(false);
        entry2.setInvoiceId(2);
        entryRepository.save(entry2);


        // Add payments for each entry
        Payment payment1 = new Payment();
        payment1.setEntry(entry1);
        payment1.setDate(Date.valueOf(LocalDate.now()));
        payment1.setAmount(BigDecimal.valueOf(100));
        payment1.setType("DEBIT");
        payment1.setMedium("Bank Transfer");
        payment1.setAutopay(false);
        // set other required fields...
        em.persist(payment1);

        Payment payment2 = new Payment();
        payment2.setEntry(entry2);
        payment2.setDate(Date.valueOf(LocalDate.now().minusDays(1)));
        payment2.setAmount(BigDecimal.valueOf(200));
        payment2.setType("CARD");
        payment2.setMedium("Online Payment");
        payment2.setAutopay(true);
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
    void testGetFilteredPredicate_byCategoryList() {
        // Set up: ensure at least two categories exist
        Entry entry1 = entryRepository.findAll().get(0);
        entry1.getBill().setCategory("catA");
        billRepository.save(entry1.getBill());

        Entry entry2 = entryRepository.findAll().get(1);
        entry2.getBill().setCategory("catB");
        billRepository.save(entry2.getBill());

        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setCategoryList(List.of("catA"));

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> "catA".equals(e.getBill().getCategory()));
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
    void testGetFilteredPredicate_byPartial() {
        // Set one entry as unpaid
        Entry entry = entryRepository.findAll().get(0);
        entry.setStatus(false);
        entryRepository.save(entry);

        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");
        filters.setPartial(true);

        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Entry.class);
        var root = cq.from(Entry.class);

        var predicate = statsHelper.getFilteredPredicate(cb, filters, root);
        cq.select(root).where(predicate);

        List<Entry> results = em.createQuery(cq).getResultList();
        assertThat(results).allMatch(e -> Boolean.FALSE.equals(e.getStatus()));
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
    void testGetMaxAvgSumQuery() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getMaxAvgSumQuery(cb, filters);

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
    void testGetTop5TypeMediumCombos() {
        EntryFilters filters = new EntryFilters();
        filters.setUserName("alice");

        var cb = em.getCriteriaBuilder();
        var cq = statsHelper.getTop5TypeMediumCombos(cb, filters);

        List<Object[]> results = em.createQuery(cq).getResultList();

        assertThat(results).isNotEmpty();
        // [0]: flow, [1]: type, [2]: medium, [3]: sum(amount)
        assertThat(results.get(0)[0]).isIn("INCOMING", "OUTGOING");
        assertThat(results.get(0)[1]).isIn("DEBIT", "CARD");
        assertThat(results.get(0)[2]).isIn("Bank Transfer", "Online Payment");
        assertThat(results.get(0)[3]).isInstanceOf(BigDecimal.class);
    }

    @Test
    void testGetTop5Categories_returnsTopCategoriesBySum() {
        // Clear all data
        entryRepository.deleteAll();
        billRepository.deleteAll();
        userRepository.deleteAll();

        // Create user
        User user = new User();
        user.setUsername("catuser");
        user.setPassword("pw");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Create bills with different categories
        Bill billA = new Bill();
        billA.setName("A");
        billA.setUser(user);
        billA.setStatus(true);
        billA.setInternal(false);
        billA.setCategory("groceries");
        billA = billRepository.save(billA);

        Bill billB = new Bill();
        billB.setName("B");
        billB.setUser(user);
        billB.setStatus(true);
        billB.setInternal(false);
        billB.setCategory("utilities");
        billB = billRepository.save(billB);

        Bill billC = new Bill();
        billC.setName("C");
        billC.setUser(user);
        billC.setStatus(true);
        billC.setInternal(false);
        billC.setCategory("entertainment");
        billC = billRepository.save(billC);

        // Add entries and payments for each bill, with unique invoiceId
        Entry entryA = new Entry();
        entryA.setBill(billA);
        entryA.setUser(user);
        entryA.setAmount(BigDecimal.valueOf(100));
        entryA.setDate(Date.valueOf(LocalDate.now()));
        entryA.setFlow("OUTGOING");
        entryA.setStatus(true);
        entryA.setOverpaid(false);
        entryA.setInvoiceId(1L);
        entryA = entryRepository.save(entryA);

        Payment paymentA = new Payment();
        paymentA.setEntry(entryA);
        paymentA.setDate(Date.valueOf(LocalDate.now()));
        paymentA.setAmount(BigDecimal.valueOf(100));
        paymentA.setType("CARD");
        paymentA.setMedium("Online");
        paymentA.setAutopay(false);
        em.persist(paymentA);

        Entry entryB = new Entry();
        entryB.setBill(billB);
        entryB.setUser(user);
        entryB.setAmount(BigDecimal.valueOf(200));
        entryB.setDate(Date.valueOf(LocalDate.now()));
        entryB.setFlow("OUTGOING");
        entryB.setStatus(true);
        entryB.setOverpaid(false);
        entryB.setInvoiceId(2L);
        entryB = entryRepository.save(entryB);

        Payment paymentB = new Payment();
        paymentB.setEntry(entryB);
        paymentB.setDate(Date.valueOf(LocalDate.now()));
        paymentB.setAmount(BigDecimal.valueOf(200));
        paymentB.setType("CARD");
        paymentB.setMedium("Online");
        paymentB.setAutopay(false);
        em.persist(paymentB);

        Entry entryC = new Entry();
        entryC.setBill(billC);
        entryC.setUser(user);
        entryC.setAmount(BigDecimal.valueOf(50));
        entryC.setDate(Date.valueOf(LocalDate.now()));
        entryC.setFlow("OUTGOING");
        entryC.setStatus(true);
        entryC.setOverpaid(false);
        entryC.setInvoiceId(3L);
        entryC = entryRepository.save(entryC);

        Payment paymentC = new Payment();
        paymentC.setEntry(entryC);
        paymentC.setDate(Date.valueOf(LocalDate.now()));
        paymentC.setAmount(BigDecimal.valueOf(50));
        paymentC.setType("CARD");
        paymentC.setMedium("Online");
        paymentC.setAutopay(false);
        em.persist(paymentC);

        em.flush();
        em.clear();

        EntryFilters filters = new EntryFilters();
        filters.setUserName("catuser");
        filters.setFlow("OUTGOING");

        var cb = em.getCriteriaBuilder();
        var cq = new StatsHelper().getTop5Categories(cb, filters);
        List<Object[]> results = em.createQuery(cq).getResultList();

        // Should be ordered by sum(amount) DESC: utilities (200), groceries (100), entertainment (50)
        assertThat(results).hasSize(3);
        assertThat(results.get(0)[0]).isEqualTo("utilities");
        assertThat((BigDecimal) results.get(0)[2]).isEqualByComparingTo("200");
        assertThat(results.get(1)[0]).isEqualTo("groceries");
        assertThat((BigDecimal) results.get(1)[2]).isEqualByComparingTo("100");
        assertThat(results.get(2)[0]).isEqualTo("entertainment");
        assertThat((BigDecimal) results.get(2)[2]).isEqualByComparingTo("50");
    }

    @Test
    void testGetTop5Categories_emptyResult() {
        entryRepository.deleteAll();
        billRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User();
        user.setUsername("emptyuser");
        user.setPassword("pw");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        EntryFilters filters = new EntryFilters();
        filters.setUserName("emptyuser");
        var cb = em.getCriteriaBuilder();
        var cq = new StatsHelper().getTop5Categories(cb, filters);
        List<Object[]> results = em.createQuery(cq).getResultList();
        assertThat(results).isEmpty();
    }

    @Test
    void testGetTop5Categories_tiesAndLimit() {
        // Only top 5 categories should be returned, ties included
        entryRepository.deleteAll();
        billRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User();
        user.setUsername("tieuser");
        user.setPassword("pw");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        for (int i = 1; i <= 6; i++) {
            Bill bill = new Bill();
            bill.setName("Bill" + i);
            bill.setUser(user);
            bill.setStatus(true);
            bill.setInternal(false);
            bill.setCategory("cat" + i);
            bill = billRepository.save(bill);
            Entry entry = new Entry();
            entry.setBill(bill);
            entry.setUser(user);
            entry.setAmount(BigDecimal.valueOf(100));
            entry.setDate(Date.valueOf(LocalDate.now()));
            entry.setFlow("OUTGOING");
            entry.setStatus(true);
            entry.setOverpaid(false);
            entry.setInvoiceId((long) i); // Ensure unique invoiceId
            entry = entryRepository.save(entry);
            Payment payment = new Payment();
            payment.setEntry(entry);
            payment.setDate(Date.valueOf(LocalDate.now()));
            payment.setAmount(BigDecimal.valueOf(100));
            payment.setType("CARD");
            payment.setMedium("Online");
            payment.setAutopay(false);
            em.persist(payment);
        }
        em.flush();
        em.clear();
        EntryFilters filters = new EntryFilters();
        filters.setUserName("tieuser");
        filters.setFlow("OUTGOING");
        var cb = em.getCriteriaBuilder();
        var cq = new StatsHelper().getTop5Categories(cb, filters);
        List<Object[]> results = em.createQuery(cq).setMaxResults(5).getResultList();
        assertThat(results).hasSize(5);
        for (Object[] row : results) {
            assertThat((BigDecimal) row[2]).isEqualByComparingTo("100");
        }
    }

}
