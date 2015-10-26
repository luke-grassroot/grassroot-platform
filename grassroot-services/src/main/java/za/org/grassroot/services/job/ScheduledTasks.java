package za.org.grassroot.services.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.org.grassroot.core.domain.Event;
import za.org.grassroot.core.dto.EventDTO;
import za.org.grassroot.core.dto.EventWithTotals;
import za.org.grassroot.core.dto.RSVPTotalsDTO;
import za.org.grassroot.core.repository.EventRepository;
import za.org.grassroot.messaging.producer.GenericJmsTemplateProducerService;
import za.org.grassroot.services.EventLogManagementService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by aakilomar on 10/5/15.
 */

@Component
public class ScheduledTasks {

    private Logger log = Logger.getLogger(getClass().getCanonicalName());

    //@Value("${reminderminutes}")
    //private int reminderminutes;

    //private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventLogManagementService eventLogManagementService;

    @Autowired
    GenericJmsTemplateProducerService jmsTemplateProducerService;

    @Scheduled(fixedRate = 300000) //runs every 5 minutes
    public void sendReminders() {
        log.info("sendReminders...starting");
        try {
            List<Event> eventList = eventRepository.findEventsForReminders();
            if (eventList != null) {
                for (Event event : eventList) {
                    log.info("sendReminders...event..." + event.getId());
                    // queue reminder request
                    jmsTemplateProducerService.sendWithNoReply("event-reminder", new EventDTO(event));
                    // update event with noreminderssent = noremindersent + 1 so we dont send it again
                    event.setNoRemindersSent(event.getNoRemindersSent() + 1);
                    event = eventRepository.save(event);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("sendReminders...done");

    }

    @Scheduled(fixedRate = 60000) //runs every 1 minutes
    public void sendVoteResults() {
        log.info("sendVoteResults...starting");
        try {
            List<Event> eventList = eventRepository.findUnsentVoteResults();
            if (eventList != null) {
                for (Event event : eventList) {
                    log.info("sendVoteResults...vote..." + event.getId());
                    // get the totals
                    RSVPTotalsDTO rsvpTotalsDTO = eventLogManagementService.getVoteResultsForEvent(event);

                    // queue vote results request
                    jmsTemplateProducerService.sendWithNoReply("vote-results", new EventWithTotals(new EventDTO(event),rsvpTotalsDTO));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("sendVoteResults...done");

    }

}
