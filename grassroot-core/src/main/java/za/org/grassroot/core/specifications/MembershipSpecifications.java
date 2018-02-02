package za.org.grassroot.core.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import za.org.grassroot.core.domain.*;
import za.org.grassroot.core.enums.Province;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MembershipSpecifications {


    private static Specification<Membership> membershipsCreatedAfter(Instant joinAfter) {
        return (root, query, cb) -> cb.greaterThan(root.get(Membership_.joinTime), joinAfter);
    }

    private static Specification<Membership> membershipsOfGroupsCreatedAfter(Instant groupCreatedAfter) {
        return (root, query, cb) -> cb.greaterThan(root.get(Membership_.group).get(Group_.createdDateTime), groupCreatedAfter);
    }

    private static Specification<Membership> membershipsOfGroupsCreatedBy(User groupCreator) {
        return (root, query, cb) -> cb.equal(root.get(Membership_.group).get(Group_.createdByUser), groupCreator);
    }

    public static Specifications<Membership> membershipsInGroups(User groupCreator, Instant groupCreatedAfter, Instant membershipCreatedAfter) {
        Specification<Membership> groupCreatedByUserSpec = MembershipSpecifications.membershipsOfGroupsCreatedBy(groupCreator);
        Specification<Membership> groupCreatedAfterSpec = MembershipSpecifications.membershipsOfGroupsCreatedAfter(groupCreatedAfter);
        Specification<Membership> membershipCreatedAfterSpec = MembershipSpecifications.membershipsCreatedAfter(membershipCreatedAfter);
        return Specifications.where(groupCreatedByUserSpec).and(groupCreatedAfterSpec).and(membershipCreatedAfterSpec);
    }

    public static Specifications<Membership> recentMembershipsInGroups(List<Group> groups, Instant membershipCreatedAfter) {
        Specification<Membership> inGroups = MembershipSpecifications.forGroups(groups);
        Specification<Membership> membershipCreatedAfterSpec = MembershipSpecifications.membershipsCreatedAfter(membershipCreatedAfter);
        return Specifications.where(inGroups).and(membershipCreatedAfterSpec).and(membershipCreatedAfterSpec);
    }

    public static Specifications<Membership> groupMembersInProvincesJoinedAfter(Group group,
                                                                                Collection<Province> provinces,
                                                                                Instant joinedDate) {
        return Specifications.where(forGroup(group)).and(memberJoinedAfter(joinedDate))
                .and(memberInProvinces(provinces));
    }

    public static Specification<Membership> filterGroupMembership(Group group,
                                                                  Collection<Province> provinces,
                                                                  Collection<String> taskTeamsUids,
                                                                  Collection<GroupJoinMethod> joinMethods,
                                                                  Integer joinDaysAgo,
                                                                  LocalDate joinDate,
                                                                  JoinDateCondition joinDaysAgoCondition){

        return (root, query, cb) -> {

            List<Predicate> restrictions = new ArrayList<>();


            if (provinces != null && provinces.size() > 0) {
                restrictions.add(root.get(Membership_.user).get(User_.province).in(provinces));
            }

            if (taskTeamsUids != null && taskTeamsUids.size() > 0) {
                restrictions.add(root.get(Membership_.group).get(Group_.uid).in(taskTeamsUids));
            } else {
                restrictions.add(cb.equal(root.get(Membership_.group), group));
            }

            if (joinMethods != null) {
                restrictions.add(root.get(Membership_.joinMethod).in(joinMethods));
            }

            LocalDate queriedJoinDate = joinDate;
            if(joinDate == null && joinDaysAgo != null){
                queriedJoinDate = LocalDate.now().minusDays(joinDaysAgo);
            }

            if (queriedJoinDate != null) {
                Instant joinDateInstant = queriedJoinDate.atStartOfDay().toInstant(ZoneOffset.UTC);
                switch (joinDaysAgoCondition){
                    case EXACT :
                        restrictions.add( cb.between(root.get(Membership_.joinTime), joinDateInstant, joinDateInstant.plus(1, ChronoUnit.DAYS)));
                        break;
                    case BEFORE:
                        restrictions.add( cb.lessThanOrEqualTo(root.get(Membership_.joinTime), joinDateInstant.plus(1, ChronoUnit.DAYS)));
                        break;
                    case AFTER:
                        restrictions.add( cb.greaterThanOrEqualTo(root.get(Membership_.joinTime), joinDateInstant));
                        break;
                }

            }

            return cb.and(restrictions.toArray(new Predicate[0]));
        };

    }

    private static Specification<Membership> hasRole(String roleName) {
        return (root, query, cb) -> cb.equal(root.get(Membership_.role).get(Role_.name), roleName);
    }

    private static Specification<Membership> forGroups(List<Group> groups) {
        return (root, query, cb) -> root.get(Membership_.group).in(groups);
    }

    public static Specification<Membership> forGroup(Group group) {
        return (root, query, cb) -> cb.equal(root.get(Membership_.group), group);
    }

    public static Specification<Membership> forGroup(String groupUid) {
        return (root, query, cb) -> cb.equal(root.get(Membership_.group).get(Group_.uid), groupUid);
    }

    public static Specifications<Membership> groupOrganizers(Group group) {
        return Specifications.where(hasRole(BaseRoles.ROLE_GROUP_ORGANIZER)).and(forGroup(group));
    }

    private static Specification<Membership> memberInProvinces(Collection<Province> provinces) {
        return (root, query, cb) -> {
            Join<Membership, User> userJoin = root.join(Membership_.user, JoinType.INNER);
            return userJoin.get(User_.province).in(provinces);
        };
    }

    private static Specification<Membership> memberJoinedAfter(Instant cutOffDateTime) {
        return (root, query, cb) -> cb.greaterThan(root.get(Membership_.joinTime), cutOffDateTime);
    }

    public static Specification<Membership> membersJoinedBefore(Instant time) {
        return (root, query, cb) -> cb.lessThan(root.get(Membership_.joinTime), time);
    }

    public static Specification<Membership> inSubgroupOf(Group group) {
        return (root, query, cb) -> {
            Join<Membership, Group> join = root.join(Membership_.group, JoinType.INNER);
            return cb.equal(join.get(Group_.parent), group);
        };
    }

    public static Specification<Membership> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get(Membership_.user), user);
    }

    public static Specifications<Membership> memberTaskTeams(Group group, User user) {
        return Specifications.where(inSubgroupOf(group)).and(hasUser(user));
    }

}
