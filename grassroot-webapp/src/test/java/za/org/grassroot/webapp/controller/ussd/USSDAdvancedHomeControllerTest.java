package za.org.grassroot.webapp.controller.ussd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import za.org.grassroot.core.domain.Group;
import za.org.grassroot.core.domain.User;
import za.org.grassroot.core.domain.geo.GeoLocation;
import za.org.grassroot.core.domain.geo.MeetingLocation;
import za.org.grassroot.core.domain.geo.ObjectLocation;
import za.org.grassroot.core.domain.task.Meeting;
import za.org.grassroot.core.domain.task.MeetingBuilder;
import za.org.grassroot.core.enums.EventType;
import za.org.grassroot.core.enums.LocationSource;
import za.org.grassroot.core.enums.UserInterfaceType;
import za.org.grassroot.integration.location.UssdLocationServicesBroker;
import za.org.grassroot.services.geo.GeographicSearchType;
import za.org.grassroot.services.geo.ObjectLocationBroker;
import za.org.grassroot.services.task.EventBroker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class USSDAdvancedHomeControllerTest extends USSDAbstractUnitTest{

    private static final String phoneForTests = "27810001111";
    private static final String testUserName = "Test User";

    private static final String advancedMenuOptionsRoot = "/ussd/more";
    private static final String phoneParameter = "msisdn";


    private static final String meetingParameter = "meetingUid";

    private static final double testLat = -11.00;
    private static final double testLong = 12.00;
    private static final Integer testRadius = 5;

    private static final User testUser = new User(phoneForTests, testUserName);
    protected final static String testGroupName = "test_group";

    protected final static Group testGroup = new Group(testGroupName, testUser);
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(USSDAdvancedHomeControllerTest.class);
    @InjectMocks
    private USSDHomeController ussdHomeController;

    @InjectMocks
    private USSDAdvancedHomeController ussdAdvancedHomeController;

    @Mock
    private UssdLocationServicesBroker ussdLocationServicesBrokerMock;

    @Mock
    private ObjectLocationBroker objectLocationBrokerMock;

    @Mock
    private EventBroker eventBroker;

    @Before
    public void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(ussdHomeController,ussdAdvancedHomeController).build();
        wireUpMessageSourceAndGroupUtil(ussdAdvancedHomeController);
    }

    @Test//(expected = URISyntaxException.class)
    @Rollback
    public void advancedUssdWelcomeMenuShouldWork() throws Exception{
        when(userManagementServiceMock.findByInputNumber(phoneForTests)).thenReturn(testUser);

        mockMvc.perform(get(advancedMenuOptionsRoot + "/start").param(phoneParameter, phoneForTests)).andExpect(status().isOk());
    }

    @Test//(expected = URISyntaxException.class)
    public void getPublicMeetingsNearUserShouldWork() throws Exception{
        GeoLocation testLocation = new GeoLocation(testLat,testLong);

        when(userManagementServiceMock.findByInputNumber(phoneForTests)).thenReturn(testUser);
        when(objectLocationBrokerMock.fetchBestGuessUserLocation(testUser.getUid())).thenReturn(testLocation);

        List<ObjectLocation> actualObjectLocations = new ArrayList<>();

        Group testGroup = new Group("test Group", testUser);

        Meeting testMeeting = new MeetingBuilder().setName("test meeting")
                .setStartDateTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .setUser(testUser).setParent(testGroup).setEventLocation("place").createMeeting();
        MeetingLocation meetingLocation = new MeetingLocation(testMeeting,testLocation,0, EventType.MEETING, LocationSource.LOGGED_APPROX);

        ObjectLocation objectLocation = new ObjectLocation(testMeeting, meetingLocation);
        actualObjectLocations.add(objectLocation);

        when(objectLocationBrokerMock.fetchMeetingLocationsNearUser(testUser,testLocation,testRadius, GeographicSearchType.PUBLIC,null))
                .thenReturn(actualObjectLocations);

        mockMvc.perform(get(advancedMenuOptionsRoot + "/public/mtgs")
                .param(phoneParameter, phoneForTests))
                .andExpect(status().is(200));

        // todo: add verifications
        verify(objectLocationBrokerMock,times(1)).fetchBestGuessUserLocation(testUser.getUid());
        verify(objectLocationBrokerMock,times(1)).fetchMeetingLocationsNearUser(testUser,testLocation,testRadius, GeographicSearchType.PUBLIC,null);
    }

    @Test//(expected = URISyntaxException.class)
    public void meetingDetailsShouldWork() throws Exception{
        Meeting testMeeting = new MeetingBuilder().setName("test meeting")
                .setStartDateTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .setUser(testUser).setParent(testGroup).setEventLocation("place").createMeeting();

        when(userManagementServiceMock.findByInputNumber(testUser.getUid())).thenReturn(testUser);

        when(eventBroker.loadMeeting(testMeeting.getUid())).thenReturn(testMeeting);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(advancedMenuOptionsRoot + "/public/mtgs/details")
                .param(phoneParameter,""+phoneForTests)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        logger.info("Meeting Details Result = {}",result.getResponse().getStatus());
    }

    @Test//(expected = URISyntaxException.class)
    public void trackUserShouldWork() throws Exception{
        // todo: actual test (MVC call)
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .get(advancedMenuOptionsRoot + "/start/track-me")
                .param(phoneParameter,""+phoneForTests)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        logger.info("Tracking User Location = {}",result.getResponse().getStatus());
        Assert.assertFalse(ussdLocationServicesBrokerMock.addUssdLocationLookupAllowed(testUser.getUid(), UserInterfaceType.USSD));
    }
}
