package za.org.grassroot.core.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Permission implements GrantedAuthority {

	PERMISSION_SEE_ALL_GROUPS,
	PERMISSION_SEE_ALL_USERS,
	PERMISSION_RESET_USER_PASSWORD,
	PERMISSION_REMOVE_USER,
	PERMISSION_CREATE_NEW_GROUP,
	PERMISSION_DELETE_GROUP,
	PERMISSION_ASSIGN_PERMISSION_TEMPLATE,
	PERMISSION_SEE_PAID_FOR_GROUPS,
	PERMISSION_DENOTE_PAID_GROUP,
	PERMISSION_REVOKE_PAID_GROUP,
	PERMISSION_VIEW_ACCOUNT_DETAILS,
	PERMISSION_UPDATE_OWN_PROFILE,

	GROUP_PERMISSION_UPDATE_GROUP_DETAILS,
	GROUP_PERMISSION_CHANGE_PERMISSION_TEMPLATE,
	GROUP_PERMISSION_FORCE_PERMISSION_CHANGE,
	GROUP_PERMISSION_CREATE_SUBGROUP,
	GROUP_PERMISSION_AUTHORIZE_SUBGROUP,
	GROUP_PERMISSION_DELEGATE_SUBGROUP_CREATION,
	GROUP_PERMISSION_DELINK_SUBGROUP,
	GROUP_PERMISSION_ADD_GROUP_MEMBER,
	GROUP_PERMISSION_FORCE_ADD_MEMBER,
	GROUP_PERMISSION_DELETE_GROUP_MEMBER,
	GROUP_PERMISSION_FORCE_DELETE_MEMBER,
	GROUP_PERMISSION_SEE_MEMBER_DETAILS,
	GROUP_PERMISSION_CREATE_GROUP_MEETING,
	GROUP_PERMISSION_VIEW_MEETING_RSVPS,
	GROUP_PERMISSION_CREATE_GROUP_VOTE,
	GROUP_PERMISSION_READ_UPCOMING_EVENTS,
	GROUP_PERMISSION_CREATE_LOGBOOK_ENTRY,
	GROUP_PERMISSION_MUTE_MEMBER,
	GROUP_PERMISSION_CLOSE_OPEN_LOGBOOK,
	GROUP_PERMISSION_ALTER_TODO;

	@Override
	public String getAuthority() {
		return name();
	}

	public String getName() {
		return name();
	}
}
