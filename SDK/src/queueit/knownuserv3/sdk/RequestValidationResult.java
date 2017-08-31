package queueit.knownuserv3.sdk;

public class RequestValidationResult {

    private String actionType;
    private String eventId;
    private String redirectUrl;
    private String queueId;

    public RequestValidationResult(String actionType, String eventId, String queueId, String redirectUrl) {
        this.actionType = actionType;
        this.eventId = eventId;
        this.queueId = queueId;
        this.redirectUrl = redirectUrl;
    }

    public String getActionType() {
        return actionType;
    }
    
    public String getEventId() {
        return eventId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean doRedirect() {
        return !Utils.isNullOrWhiteSpace(redirectUrl);
    }

    public String getQueueId() {
        return this.queueId;
    }
}

class KnowUserException extends Exception {

    public KnowUserException(String message) {
        super(message);
    }

    public KnowUserException(String message, Throwable thrwbl) {
        super(message, thrwbl);
    }
}
