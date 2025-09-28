package com.projects.bills.Specifications;

import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

public class BillSpecification {
    public static Specification<Bill> filterBills(Boolean status, String category, Boolean internal, User user) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate, cb.equal(root.get("user"), user));
            predicate = cb.and(predicate, cb.isNull(root.get("recycleDate")));
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (category != null && !category.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            if (internal != null) {
                predicate = cb.and(predicate, cb.equal(root.get("internal"), internal));
            }
            assert query != null;
            query.orderBy(cb.asc(root.get("name")));
            return predicate;
        };
    }
}
