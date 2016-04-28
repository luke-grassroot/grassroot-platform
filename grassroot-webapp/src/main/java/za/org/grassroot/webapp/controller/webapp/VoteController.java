package za.org.grassroot.webapp.controller.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.org.grassroot.core.domain.*;
import za.org.grassroot.core.dto.ResponseTotalsDTO;
import za.org.grassroot.core.enums.EventRSVPResponse;
import za.org.grassroot.services.*;
import za.org.grassroot.webapp.controller.BaseController;
import za.org.grassroot.webapp.model.web.EventWrapper;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static za.org.grassroot.core.domain.Permission.GROUP_PERMISSION_CREATE_GROUP_VOTE;

/**
 * Created by luke on 2015/10/30.
 */
@Controller
@RequestMapping("/vote/")
@SessionAttributes("vote")
public class VoteController extends BaseController {

    Logger log = LoggerFactory.getLogger(VoteController.class);

    @Autowired
    private EventBroker eventBroker;

    @Autowired
    private PermissionBroker permissionBroker;

    @Autowired
    private GroupBroker groupBroker;

    @Autowired
    EventManagementService eventManagementService;

    @Autowired
    EventLogManagementService eventLogManagementService;

    @RequestMapping("create")
    public String createVote(Model model, @RequestParam(value="groupUid", required = false) String groupUid) {

        boolean groupSpecified = (groupUid != null);
        User user = userManagementService.load(getUserProfile().getUid()); // in case permissions changed, call entity from DB

        // todo: not sre if we even need this ...
        EventWrapper voteWrapper = EventWrapper.makeEmpty(true);

        if (groupSpecified) {
            Group group = groupBroker.load(groupUid);
            permissionBroker.validateGroupPermission(user, group, GROUP_PERMISSION_CREATE_GROUP_VOTE); // double check, given sensitivity
            voteWrapper.setParentUid(groupUid);
            model.addAttribute("group", group);
        } else {
            model.addAttribute("possibleGroups", permissionBroker.getActiveGroups(user, GROUP_PERMISSION_CREATE_GROUP_VOTE));
        }

        model.addAttribute("vote", voteWrapper);

        return "vote/create";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String createVoteDo(Model model, @ModelAttribute("vote") EventWrapper vote, BindingResult bindingResult,
                               @RequestParam(value = "selectedGroupUid", required = false) String selectedGroupUid,
                               HttpServletRequest request, RedirectAttributes redirectAttributes) {

        log.info("Vote passed back to us: " + vote.toString());
        String groupUid = (selectedGroupUid == null) ? vote.getParentUid() : selectedGroupUid;

        eventBroker.createVote(getUserProfile().getUid(), groupUid, JpaEntityType.GROUP, vote.getTitle(), vote.getEventDateTime(),
                               vote.isIncludeSubGroups(), vote.isRelayable(), vote.getDescription(), Collections.emptySet());

        log.info("Stored vote, at end of creation: " + vote.toString());

        // todo : ask person who called vote for vote (and, in general, add that to group view)
        addMessage(redirectAttributes, MessageType.SUCCESS, "vote.creation.success", request);
        redirectAttributes.addAttribute("groupUid", groupUid);
        return "redirect:/group/view";
    }

    @RequestMapping("view")
    public String viewVote(Model model, @RequestParam String eventUid) {

        Event vote = eventBroker.load(eventUid);
        boolean canModify = (vote.getCreatedByUser().equals(getUserProfile())
                && vote.getEventStartDateTime().isAfter(Instant.now())); // todo: make this more nuanced

        ResponseTotalsDTO responses = eventManagementService.getVoteResultsDTO(vote);

        model.addAttribute("vote", new EventWrapper(vote));
        model.addAttribute("yes", responses.getYes());
        model.addAttribute("no", responses.getNo());
        model.addAttribute("abstained", responses.getMaybe());
        model.addAttribute("noreply", responses.getNumberNoRSVP());
        model.addAttribute("replied", responses.getNumberOfUsers() - responses.getNumberNoRSVP());
        model.addAttribute("possible", responses.getNumberOfUsers());

        if (canModify) {
            model.addAttribute("canModify", true);
            model.addAttribute("reminderOptions", EventReminderType.values());
            model.addAttribute("customReminderOptions", reminderMinuteOptions(false));
        }

        return "vote/view";
    }

    @RequestMapping(value = "description", method = RequestMethod.POST)
    public String changeDescription(Model model, @ModelAttribute("vote") Vote vote, HttpServletRequest request) {
        log.info("Vote we are passed back ... " + vote);
        eventBroker.updateVote(getUserProfile().getUid(), vote.getUid(), vote.getEventDateTimeAtSAST(), vote.getDescription());
        addMessage(model, MessageType.SUCCESS, "vote.update.done", request);
        return viewVote(model, vote.getUid());
    }

    @RequestMapping(value = "changedatetime", method = RequestMethod.POST)
    public String changeDateTime(Model model, @ModelAttribute("vote") Vote vote, HttpServletRequest request) {
        // note: this is pretty much the same as above, but may make it more sophisticated later, so am keeping separate
        Vote changedVote = eventBroker.updateVote(getUserProfile().getUid(), vote.getUid(),
                                                  vote.getEventDateTimeAtSAST(), vote.getDescription());
        String toDisplay = DateTimeFormatter.ofPattern("h:mm a' on 'E, d MMMM").
                format(changedVote.getEventDateTimeAtSAST());
        addMessage(model, MessageType.SUCCESS, "vote.update.closing.done", new String[] {toDisplay}, request);
        return viewVote(model, vote.getUid());
    }

    @RequestMapping(value = "reminder", method = RequestMethod.POST)
    public String changeReminderSettings(Model model, @RequestParam String eventUid, @RequestParam EventReminderType reminderType,
                                         @RequestParam(value = "custom_minutes", required = false) Integer customMinutes, HttpServletRequest request) {

        int minutes = (customMinutes != null && reminderType.equals(EventReminderType.CUSTOM)) ? customMinutes : 0;
        eventBroker.updateReminderSettings(getUserProfile().getUid(), eventUid, reminderType, minutes);
        Instant newScheduledTime = eventBroker.load(eventUid).getScheduledReminderTime();
        if (newScheduledTime != null) {
            // todo: make sure this actually works properly with zones, on AWS in Ireland, etc ...
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a' on 'E, d MMMM").withZone(ZoneId.systemDefault());
            String reminderTime = formatter.format(newScheduledTime);
            addMessage(model, MessageType.SUCCESS, "vote.reminder.changed", new String[] {reminderTime}, request);
        } else {
            addMessage(model, MessageType.SUCCESS, "vote.reminder.none", request);
        }
        return viewVote(model, eventUid);
    }

    @RequestMapping(value = "answer", method = RequestMethod.GET)
    public String answerVote(Model model, @RequestParam String eventUid, @RequestParam String answer,
                             HttpServletRequest request, RedirectAttributes redirectAttributes) {

        Event vote = eventBroker.load(eventUid);
        User sessionUser = getUserProfile();
        String priorUrl = request.getHeader(HttpHeaders.REFERER);

        eventLogManagementService.rsvpForEvent(vote, sessionUser, EventRSVPResponse.fromString(answer));

        addMessage(redirectAttributes, MessageType.INFO, "vote.recorded", request);

        if (priorUrl.contains("group")) {
            redirectAttributes.addAttribute("groupUid", vote.resolveGroup().getUid());
            return "redirect:/group/view";
        } else if (priorUrl.contains("vote")) {
            redirectAttributes.addAttribute("eventUid", eventUid);
            return "redirect:/vote/view";
        } else {
            return "redirect:/home";
        }

    }

}
