package za.org.grassroot.webapp.controller.rest.location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.org.grassroot.core.domain.User;
import za.org.grassroot.core.domain.geo.GeoLocation;
import za.org.grassroot.core.domain.geo.ObjectLocation;
import za.org.grassroot.core.domain.livewire.LiveWireAlert;
import za.org.grassroot.core.repository.UserRepository;
import za.org.grassroot.services.geo.GeographicSearchType;
import za.org.grassroot.services.geo.ObjectLocationBroker;
import za.org.grassroot.services.livewire.LiveWireAlertBroker;
import za.org.grassroot.webapp.enums.RestMessage;
import za.org.grassroot.webapp.model.rest.wrappers.ResponseWrapper;
import za.org.grassroot.webapp.util.RestUtil;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Api("/api/location")
@RequestMapping(value = "/api/location")
public class AroundMeRestController {

    private final ObjectLocationBroker objectLocationBroker;
    private final UserRepository userRepository;
    private final LiveWireAlertBroker liveWireAlertBroker;

    @Autowired
    public AroundMeRestController(ObjectLocationBroker objectLocationBroker,UserRepository userRepository,LiveWireAlertBroker liveWireAlertBroker){
        this.objectLocationBroker = objectLocationBroker;
        this.userRepository = userRepository;
        this.liveWireAlertBroker = liveWireAlertBroker;
    }

    @RequestMapping(value = "/all/{userUid}", method = RequestMethod.GET)
    @ApiOperation(value = "All entities near user", notes = "Fetch all public groups, public meetings, " +
            "and public alerts near to the user, as well as those entities the user belongs to")
    public ResponseEntity<List<ObjectLocation>> fetchAllEntitiesNearUser(@PathVariable String userUid,
                                                                         @RequestParam double longitude,
                                                                         @RequestParam double latitude,
                                                                         @RequestParam int radiusMetres,
                                                                         @ApiParam(value = "An optional term to filter by " +
                                                                                 "name (subject etc) of the entities")
                                                                         @RequestParam(required = false) String filterTerm) {
        GeoLocation location = new GeoLocation(latitude,longitude);
        User user = userRepository.findOneByUid(userUid);

        Set<ObjectLocation> objectLocationSet = new HashSet<>();

        objectLocationSet.addAll(objectLocationBroker.fetchGroupsNearby(userUid, location,radiusMetres, filterTerm, GeographicSearchType.PUBLIC)); //Adding Groups near user
        objectLocationSet.addAll(objectLocationBroker.fetchMeetingLocationsNearUser(user, location, radiusMetres, GeographicSearchType.PUBLIC, null)); // Adding Meetings near user

        List<ObjectLocation> list = new ArrayList<>(objectLocationSet);

        return ResponseEntity.ok(list);
    }

    @RequestMapping(value = "/all/groups/{userUid}", method = RequestMethod.GET)
    @ApiOperation(value = "All groups near user", notes = "Fetch all groups near to the user, both those they belong to" +
            " and those they don't")
    public ResponseEntity<List<ObjectLocation>> fetchGroupsNearUser(@PathVariable String userUid,
                                                                    @RequestParam double longitude,
                                                                    @RequestParam double latitude,
                                                                    @RequestParam int radiusMetres,
                                                                    @RequestParam(required = false) String filterTerm){
        GeoLocation location = new GeoLocation(latitude,longitude);
        return ResponseEntity.ok(objectLocationBroker.fetchGroupsNearby(userUid,location,radiusMetres,filterTerm,
                GeographicSearchType.PUBLIC));
    }

    @RequestMapping(value = "/all/alerts/{userUid}", method = RequestMethod.GET)
    @ApiOperation(value = "All public alerts near user", notes = "Fetch all public alerts near to the user")
    public ResponseEntity<List<LiveWireAlert>> getAlertsNearUser(@PathVariable String userUid,
                                                                  @RequestParam double longitude,
                                                                  @RequestParam double latitude,
                                                                  @RequestParam int radiusMetres,
                                                                  @RequestParam String createdByMe){
        GeoLocation location = new GeoLocation(latitude,longitude);
        List<LiveWireAlert> liveWireAlerts = liveWireAlertBroker.fetchAlertsNearUser(userUid,location,createdByMe,radiusMetres, GeographicSearchType.PUBLIC);
        return ResponseEntity.ok(liveWireAlerts);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ResponseWrapper> invalidLocationPassed() {
        return RestUtil.errorResponse(HttpStatus.BAD_REQUEST, RestMessage.LOCATION_EMPTY);
    }



    // do similar with a method that looks for only public groups, or only public meetings

    // and have a method that sends back both the public entities, and the entities private to the user (e.g.,
    // meetings of groups they are part of, where meeting or group is within the radius)

}