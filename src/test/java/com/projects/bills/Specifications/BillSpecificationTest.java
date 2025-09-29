package com.projects.bills.Specifications;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class BillSpecificationTest {
    private Root<Bill> root;
    private CriteriaQuery<Object> query;
    private CriteriaBuilder cb;
    private User user;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
        user = new User();
        user.setUsername("alice");
    }

    @Test
    void testFilterBills_AllNullExceptUser() {
        Specification<Bill> spec = BillSpecification.filterBills(null, null, null, user);
        Predicate conjunction = mock(Predicate.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate recyclePredicate = mock(Predicate.class);
        jakarta.persistence.criteria.Order order = mock(jakarta.persistence.criteria.Order.class);

        when(cb.conjunction()).thenReturn(conjunction);
        when(cb.equal(root.get("user"), user)).thenReturn(userPredicate);
        when(cb.isNull(root.get("recycleDate"))).thenReturn(recyclePredicate);
        when(cb.and(conjunction, userPredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, recyclePredicate)).thenReturn(conjunction);
        when(cb.asc(root.get("name"))).thenReturn(order);
        when(query.orderBy(order)).thenReturn(query);

        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
        verify(cb).equal(root.get("user"), user);
        verify(cb).isNull(root.get("recycleDate"));
        verify(cb).asc(root.get("name"));
        verify(query).orderBy(order);
    }

    @Test
    void testFilterBills_WithStatus() {
        Specification<Bill> spec = BillSpecification.filterBills(true, null, null, user);
        Predicate conjunction = mock(Predicate.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate recyclePredicate = mock(Predicate.class);
        Predicate statusPredicate = mock(Predicate.class);

        when(cb.conjunction()).thenReturn(conjunction);
        when(cb.equal(root.get("user"), user)).thenReturn(userPredicate);
        when(cb.isNull(root.get("recycleDate"))).thenReturn(recyclePredicate);
        when(cb.equal(root.get("status"), true)).thenReturn(statusPredicate);
        when(cb.and(conjunction, userPredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, recyclePredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, statusPredicate)).thenReturn(conjunction);
        when(query.orderBy(anyList())).thenReturn(query);

        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).equal(root.get("status"), true);
    }

    @Test
    void testFilterBills_WithCategory() {
        Specification<Bill> spec = BillSpecification.filterBills(null, "utilities", null, user);
        Predicate conjunction = mock(Predicate.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate recyclePredicate = mock(Predicate.class);
        Predicate categoryPredicate = mock(Predicate.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Path categoryPath = mock(jakarta.persistence.criteria.Path.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Expression<String> lowerCategoryPath = mock(jakarta.persistence.criteria.Expression.class);

        when(cb.conjunction()).thenReturn(conjunction);
        when(cb.equal(root.get("user"), user)).thenReturn(userPredicate);
        when(cb.isNull(root.get("recycleDate"))).thenReturn(recyclePredicate);
        when(root.get(eq("category"))).thenReturn(categoryPath);
        when(cb.lower(categoryPath)).thenReturn(lowerCategoryPath);
        when(cb.equal(lowerCategoryPath, "utilities")).thenReturn(categoryPredicate);
        when(cb.and(conjunction, userPredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, recyclePredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, categoryPredicate)).thenReturn(conjunction);
        when(query.orderBy(anyList())).thenReturn(query);

        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).lower(categoryPath);
        verify(cb).equal(lowerCategoryPath, "utilities");
    }

    @Test
    void testFilterBills_WithInternal() {
        Specification<Bill> spec = BillSpecification.filterBills(null, null, true, user);
        Predicate conjunction = mock(Predicate.class);
        Predicate userPredicate = mock(Predicate.class);
        Predicate recyclePredicate = mock(Predicate.class);
        Predicate internalPredicate = mock(Predicate.class);

        when(cb.conjunction()).thenReturn(conjunction);
        when(cb.equal(root.get("user"), user)).thenReturn(userPredicate);
        when(cb.isNull(root.get("recycleDate"))).thenReturn(recyclePredicate);
        when(cb.equal(root.get("internal"), true)).thenReturn(internalPredicate);
        when(cb.and(conjunction, userPredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, recyclePredicate)).thenReturn(conjunction);
        when(cb.and(conjunction, internalPredicate)).thenReturn(conjunction);
        when(query.orderBy(anyList())).thenReturn(query);

        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).equal(root.get("internal"), true);
    }
}
