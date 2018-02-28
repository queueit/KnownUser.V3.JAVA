package queueit.knownuserv3.sdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RequestValidationResult {

    private String actionType;
    private String eventId;
    private String redirectUrl;
    private String queueId;
    private String redirectType;
    public boolean isAjaxResult;

    public RequestValidationResult(String actionType, String eventId, String queueId, String redirectUrl, String redirectType) {
        this.actionType = actionType;
        this.eventId = eventId;
        this.queueId = queueId;
        this.redirectUrl = redirectUrl;
        this.redirectType = redirectType;
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

    public String getRedirectType() {
        return this.redirectType;
    }

    public String getAjaxQueueRedirectHeaderKey() {
        return "x-queueit-redirect";
    }

    public String getAjaxRedirectUrl() {
        try {
            if (!Utils.isNullOrWhiteSpace(redirectUrl)) {
                return URLEncoder.encode(redirectUrl, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {           
        }
        return "";
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
