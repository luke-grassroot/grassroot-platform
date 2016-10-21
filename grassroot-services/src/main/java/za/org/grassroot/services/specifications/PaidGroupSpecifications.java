package za.org.grassroot.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import za.org.grassroot.core.domain.Group;
import za.org.grassroot.core.domain.PaidGroup;
import za.org.grassroot.core.domain.PaidGroup_;

import java.time.Instant;

/**
 * Created by luke on 2016/10/21.
 */
public final class PaidGroupSpecifications {

    public static Specification<PaidGroup> isForGroup(final Group group) {
        return (root, query, cb) -> cb.equal(root.get(PaidGroup_.group), group);
    }

    public static Specification<PaidGroup> expiresAfter(final Instant time) {
        return (root, query, cb) -> cb.greaterThan(root.get(PaidGroup_.expireDateTime), time);
    }

}
