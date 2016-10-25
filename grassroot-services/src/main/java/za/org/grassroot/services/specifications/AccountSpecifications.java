package za.org.grassroot.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import za.org.grassroot.core.domain.Account;
import za.org.grassroot.core.domain.Account_;

import java.time.Instant;

/**
 * Created by luke on 2016/10/25.
 */
public final class AccountSpecifications {

    public static Specification<Account> isValidBetween(Instant start, Instant end) {
        return (root, query, cb) -> cb.and(cb.greaterThan(root.get(Account_.createdDateTime), start),
                    cb.lessThan(root.get(Account_.disabledDateTime), end));
    }

}
