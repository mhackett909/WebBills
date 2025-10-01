package com.projects.bills.Specifications;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.List;

public class BillSpecification {
    public static Specification<Bill> filterBills(Boolean status, User user) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate, cb.equal(root.get("user"), user));
            predicate = cb.and(predicate, cb.isNull(root.get("recycleDate")));
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            assert query != null;
            query.orderBy(cb.asc(root.get("name")));
            return predicate;
        };
    }
}
