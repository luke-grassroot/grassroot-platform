package za.org.grassroot.webapp.enums;

public enum RestMessage {

    VERIFICATION_TOKEN_SENT,
    INVALID_OTP,
    INVALID_TOKEN,
    INVALID_MSISDN,
    INVALID_USERNAME,
    INVALID_PARAMETER,
    TOKEN_EXPIRED,
    LOGIN_SUCCESS,
    USER_REGISTRATION_SUCCESSFUL,
    USER_REGISTRATION_FAILED,
    USER_ALREADY_EXISTS,
    USER_DOES_NOT_EXIST,
    USER_HAS_NO_GROUPS,
    USER_HAS_NO_TASKS,
    USER_PROFILE,
    USER_GROUPS,
    LOCATION_RECORDED,
    USER_OKAY,
    OTP_REQ_BEFORE_ADD,
    OTP_REQUIRED,
    USER_LOGGED_OUT,
    TOKEN_EXTENDED,
    TOKEN_STILL_VALID,

    GROUP_CREATED,
    GROUP_DUPLICATE_CREATE,
    GROUP_BAD_PHONE_NUMBER,
    GROUP_NOT_CREATED,
    GROUP_JOIN_REQUEST_SENT,
    GROUP_NOT_FOUND,
    POSSIBLE_GROUP_MATCHES,
    GROUP_FOUND,
    NO_GROUP_MATCHING_TERM_FOUND,
    GROUP_PROFILE,

    JOIN_REQUESTS,
    USER_ALREADY_PART_OF_GROUP,
    REQUEST_MISSING_UID,
    GROUP_JOIN_RESPONSE_PROCESSED,
    GROUP_JOIN_REQUEST_CANCELLED,
    GROUP_JOIN_REQUEST_REMIND,
    GROUP_JOIN_REQUEST_NOT_FOUND,

    UPDATED,
    APPROVER_PERMISSIONS_CHANGED,
    GROUP_RENAMED,
    GROUP_DESCRIPTION_CHANGED,
    GROUP_DISCOVERABLE_UPDATED,
    GROUP_JOIN_CODE_OPENED,
    GROUP_JOIN_CODE_CLOSED,
    GROUP_PINNED,
    GROUP_UNPINNED,
    PERMISSIONS_RETURNED,
    PERMISSIONS_UPDATED,
    MEMBER_ROLE_CHANGED,
    NO_MEMBERS_SENT,

    USER_ACTIVITIES,
    GROUP_ACTIVITIES,
    NO_GROUP_ACTIVITIES,
    TASK_DETAILS,
    TASK_NOT_FOUND,

    GROUP_MEMBERS, // assuming this is not an error, moving here
    PARENT_MEMBERS,
    MEMBERS_ADDED,
    MEMBERS_REMOVED,
    MEMBER_UNSUBSCRIBED,
    MEMBER_ALREADY_LEFT,
    GROUP_SIZE_LIMIT,
    MEMBER_ALIAS_CHANGED,
    MEMBER_ALIAS_RETURNED,
    GROUP_LANGUAGE_CHANGED,
    SOLE_ORGANIZER,

    USER_HAS_ALREADY_VOTED,
    VOTE_CLOSED,
    VOTE_CANCELLED,
    VOTE_SENT,
    VOTE_DETAILS,
    VOTE_CREATED,
    VOTE_DETAILS_UPDATED,
    USER_NOT_PART_OF_VOTE,
    VOTE_RECORDED,

    MEETING_CANCELLED,
    MEETING_PAST,
    MEETING_CREATED,
    MEETING_DETAILS,
    MEETING_DETAILS_UPDATED,
    MEETING_PUBLIC_UPDATED,
    MEETING_UPDATE_ERROR,
    RSVP_SENT,
    PAST_DUE,
    MEETING_IMAGE_ADDED,
    MEETING_IMAGE_ERROR,
    EVENT_LIMIT_REACHED,

    TODO_CREATED,
    TODO_LIMIT_REACHED,
    TODO_SET_COMPLETED,
    TODO_ALREADY_COMPLETED,
    TODO_TYPE_MISMATCH,
    TODO_RESPONSE_RECORDED,

    TASK_FOUND,
    TASK_NO_IMAGES,
    TASK_IMAGES_FOUND,
    TASK_IMAGE_ERROR,
    TASK_IMAGE_COUNT,
    TASK_IMAGE_RECORD,

    INVALID_PAGE_NUMBER,
    EXCEEDS_TOTAL_PAGES,
    TIME_CANNOT_BE_IN_THE_PAST,
    CLIENT_ERROR,
    ERROR,
    REGISTERED_FOR_PUSH,

    NOTIFICATIONS,
    NOTIFICATIONS_FINISHED,
    INVALID_INPUT,
    INVALID_ENTITY_TYPE,
    PERMISSION_DENIED,

    PROFILE_SETTINGS_UPDATED,
    PROFILE_SETTINGS,
    PROFILE_SETTINGS_ERROR,
    INVALID_LANG_CODE,

    NOTIFICATION_UPDATED,
    ALREADY_UPDATED,
    DEREGISTERED_FOR_PUSH,
    USER_NOT_REGISTERED_FOR_PUSH,
    VOTE_ALREADY_CLOSED,
    MEETING_ALREADY_CANCELLED,
    TODO_UPDATED,
    TODO_CANCELLED,

    UPLOADED,
    ALREADY_EXISTS,
    BAD_PICTURE_FORMAT,
    PICTURE_NOT_RECEIVED,
    PICTURE_REMOVED,
    PICTURE_NOT_FOUND,
    IMAGE_FACES_UPDATED,
    IMAGE_RECORD_REMOVED,

    DATE_TIME_PARSED,
    DATE_TIME_FAILED,

    CHAT_SENT,
    CHAT_DEACTIVATED,
    CHAT_ACTIVATED,
    MESSAGE_SETTING_NOT_FOUND,
    MESSENGER_SETTINGS,
    PING,
    CHATS_MARKED_AS_READ, EMPTY_LIST,

    USER_NO_ACCOUNT,
    ACCOUNT_CREATED,
    PAYMENT_STARTED,
    ACCOUNT_DISABLED,
    ACCOUNT_ENABLED,
    GROUP_ADDED_TO_ACCOUNT,
    FFORM_MESSAGE_SENT,
    ACCOUNT_GROUPS,
    GROUP_REMOVED_ACCOUNT,

    PAYMENT_ERROR,
    ACCOUNT_GROUPS_EXHAUSTED,
    GROUP_ALREADY_PAID_FOR,
    GROUP_ACCOUNT_WRONG,
    GROUP_NOT_PAID_FOR,
    FREE_FORM_EXHAUSTED,
    ACCOUNT_TYPE_CHANGED,
    MUST_REMOVE_PAID_GROUPS,
    ERROR_REMOVING_GROUP,
    MISC_PAYMENT_ERROR,
    BAD_TOKEN_UPDATE,

    INTERNAL_SERVER_ERROR,
    FILE_GENERATION_ERROR,

    LOCATION_HAS_MEETINGS,
    LOCATION_EMPTY,
    LOCATION_NOT_FOUND,
    INVALID_LOCATION_RADIUS_PARAMETER,
    INVALID_LOCATION_RESTRICTION_PARAMETER,
    INVALID_LOCATION_LATLONG_PARAMETER,
    INVALID_BOUNDINGBOX_LATLONG_PARAMETER,

    LIVEWIRE_ALERT_CREATED,

    CAMPAIGN_FOUND,
    CAMPAIGN_NOT_FOUND,
    CAMPAIGN_CREATED,
    CAMPAIGN_MESSAGE_ADDED,
    CAMPAIGN_TAG_ADDED,
    CAMPAIGN_MESSAGE_TAG_ADDED,
    CAMPAIGN_WITH_SAME_CODE_EXIST,
    CAMPAIGN_MESSAGE_ACTION_ADDED,
    CAMPAIGN_VALIDATION_ERROR,

    CAMPAIGN_CREATION_INVALID_INPUT,
    CAMPAIGN_MESSAGE_CREATION_INVALID_INPUT,
    CAMPAIGN_MESSAGE_ACTION_CREATION_INVALID_INPUT,

    JOIN_WORD_TAKEN,

    INVALID_PASSWORD,
    PASSWORD_RESET,
    INTERFACE_NOT_OPEN
}
