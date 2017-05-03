package za.org.grassroot.services.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.org.grassroot.core.domain.geo.GeoLocation;
import za.org.grassroot.core.domain.geo.ObjectLocation;

import javax.persistence.EntityManager;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ObjectLocationBrokerImpl implements ObjectLocationBroker {
    private final static double MIN_LATITUDE = -90.00;
    private final static double MAX_LATITUDE = 90.00;
    private final static double MIN_LONGITUDE = -180.00;
    private final static double MAX_LONGITUDE = 180.00;

    private static final Logger logger = LoggerFactory.getLogger(ObjectLocationBroker.class);
    private final EntityManager entityManager;

    @Autowired
    public ObjectLocationBrokerImpl (EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * TODO: 1) Use the user restrictions and search for public groups
     * TODO: 2) Use the radius to search
     */
    @Override
    @Transactional(readOnly = true)
    public List<ObjectLocation> fetchGroupLocations (GeoLocation location, Integer radius) throws InvalidParameterException {
        logger.info("Fetching group locations ...");

        assertRadius(radius);
        assertGeolocation(location);

        List<ObjectLocation> list = entityManager.createQuery("SELECT NEW za.org.grassroot.core.domain.geo.ObjectLocation( " +
                        "g.uid, g.groupName, l.location.latitude, " + "l.location.longitude, l.score, 'GROUP', g.description) " +
                        "FROM GroupLocation l " +
                        "INNER JOIN l.group g " +
                        "WHERE g.discoverable = true " +
                        "AND l.localDate <= :date " +
                        "AND l.localDate = (SELECT MAX(ll.localDate) FROM GroupLocation ll WHERE ll.group = l.group)",
                ObjectLocation.class).setParameter("date", LocalDate.now()).getResultList();

        return (list.isEmpty() ? new ArrayList<>() : list);
    }

    /**
     * TODO: 1) Use the user restrictions and search for public groups/meetings
     * TODO: 2) Use the radius to search
     */
    @Override
    @Transactional(readOnly = true)
    public List<ObjectLocation> fetchMeetingLocations (GeoLocation location, Integer radius) {
        logger.info("Fetching meeting locations ...");

        assertRadius(radius);
        assertGeolocation(location);

        List<ObjectLocation> list = entityManager.createQuery("SELECT NEW za.org.grassroot.core.domain.geo.ObjectLocation(" +
                        "m.uid, m.name, l.location.latitude, l.location.longitude, l.score, 'MEETING', " +
                        "CONCAT('<strong>Where: </strong>', m.eventLocation, '<br/><strong>Date and Time: </strong>', m.eventStartDateTime)) " +
                        "FROM MeetingLocation l " +
                        "INNER JOIN l.meeting m " +
                        "WHERE m.isPublic = true " +
                        "AND l.calculatedDateTime <= :date " +
                        "AND l.calculatedDateTime = (SELECT MAX(ll.calculatedDateTime) FROM MeetingLocation ll WHERE ll.meeting = l.meeting) ",
                ObjectLocation.class).setParameter("date", Instant.now()).getResultList();

        return (list.isEmpty() ? new ArrayList<>() : list);
    }

    /**
     * TODO: IS IT NECESSARY?
     * TODO: 1) Use the user restrictions and search for public groups/meetings
     * TODO: 2) Use the radius to search
     * TODO: 3) Validate ObjectLocation/group
     */
    @Override
    public List<ObjectLocation> fetchMeetingLocationsByGroup (ObjectLocation group, GeoLocation location, Integer radius) {
        logger.info("Fetching meeting locations by group ...");

        assertRadius(radius);
        assertGeolocation(location);

        List<ObjectLocation> list = entityManager.createQuery("SELECT NEW za.org.grassroot.core.domain.geo.ObjectLocation(" +
                        "m.uid, m.name, l.location.latitude, l.location.longitude, l.score, 'MEETING', " +
                        "CONCAT('<strong>Where: </strong>', m.eventLocation, '<br/><strong>Date and Time: </strong>', m.eventStartDateTime)) " +
                        "FROM Meeting m " +
                        "INNER JOIN m.parentGroup g, GroupLocation l " +
                        "WHERE l.localDate <= :date " +
                        "AND l.group = g " +
                        "AND g.uid = :guid " +
                        "AND l.localDate = (SELECT MAX(ll.localDate) FROM GroupLocation ll WHERE ll.group = l.group)",
                ObjectLocation.class).setParameter("date", LocalDate.now()).setParameter("guid", group.getUid()).getResultList();

        return (list.isEmpty() ? new ArrayList<>() : list);
    }

    private void assertRadius (Integer radius) throws InvalidParameterException {
        if (radius == null || radius <= 0) {
            throw new InvalidParameterException("Invalid radius object.");
        }
    }
    private void assertGeolocation (GeoLocation location) throws InvalidParameterException {
        if (location == null || location.getLatitude() < MIN_LATITUDE || location.getLatitude() > MAX_LATITUDE ||
                location.getLongitude() < MIN_LONGITUDE || location.getLongitude() > MAX_LONGITUDE) {
            throw new InvalidParameterException("Invalid GeoLocation object.");
        }
    }
}
