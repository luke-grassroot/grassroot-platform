package za.org.grassroot.core.domain;

import za.org.grassroot.core.enums.EventType;
import za.org.grassroot.core.enums.MeetingImportance;
import za.org.grassroot.core.util.UIDGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@DiscriminatorValue("MEETING")
public class Meeting extends Event<MeetingContainer> implements VoteContainer {

	@Column(name = "location", length = 50)
	private String eventLocation;

	@Column(name="importance")
	@Enumerated(EnumType.STRING)
	private MeetingImportance importance;

	// note : for the moment we only have this in meeting, since meeting request has no need (public is only asked after
	// the meeting is confirmed & done, and vote has no public -- so column is just redundant and false on others)
	@Column(name="public")
	private boolean publicMtg;

	private Meeting() {
		// for JPA
	}

	public Meeting(String name, Instant startDateTime, User user, MeetingContainer parent, String eventLocation) {
		this(name, startDateTime, user, parent, eventLocation, false);
	}

	public Meeting(String name, Instant startDateTime, User user, MeetingContainer parent, String eventLocation, boolean includeSubGroups) {
		this(name, startDateTime, user, parent, eventLocation, includeSubGroups, EventReminderType.DISABLED, 0, null, null);
	}

	// production constructor : above are only used in tests
	public Meeting(String name, Instant startDateTime, User user, MeetingContainer parent, String eventLocation, boolean includeSubGroups,
				   EventReminderType reminderType, int customReminderMinutes, String description, MeetingImportance importance) {
		super(startDateTime, user, parent, name, includeSubGroups, reminderType, customReminderMinutes, description, true, false);
		this.eventLocation = Objects.requireNonNull(eventLocation);
		this.importance = importance == null ? MeetingImportance.ORDINARY : importance;
		setScheduledReminderActive(true);
		setParent(parent);
	}

	public static Meeting makeEmpty(User user) {
		Meeting meeting = new Meeting();
		meeting.uid = UIDGenerator.generateId();
		meeting.setCreatedByUser(user);
		return meeting;
	}

	@Override
	public EventType getEventType() {
		return EventType.MEETING;
	}

	@Override
	public JpaEntityType getJpaEntityType() {
		return JpaEntityType.MEETING;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}

	public MeetingImportance getImportance() {
		return importance;
	}

	public void setImportance(MeetingImportance importance) { this.importance = importance; }

	public boolean isPublicMtg() {
		return publicMtg;
	}

	public void setPublicMtg(boolean publicMtg) {
		this.publicMtg = publicMtg;
	}

	public MeetingContainer getParent() {
		if (parentGroup != null) {
			return parentGroup;
		} else if (parentTodo != null) {
			return parentTodo;
		} else {
			throw new IllegalStateException("There is no " + MeetingContainer.class.getSimpleName() + " parent defined for " + this);
		}
	}

	public void setParent(MeetingContainer parent) {
		if (parent instanceof Group) {
			this.parentGroup = (Group) parent;
			this.parentGroup.addChildEvent(this); // needed for double sided relationship in JPA
		} else if (parent instanceof Todo) {
			this.parentTodo = (Todo) parent;
		} else {
			throw new UnsupportedOperationException("Unsupported parent: " + parent);
		}
	}
}
