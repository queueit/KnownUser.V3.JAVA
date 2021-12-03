package com.queue_it.connector;

public class CancelEventConfig {

    private String eventId;
    private String queueDomain;
    private String cookieDomain;
    private int version;
    private String actionName = "unspecified";
    private Boolean isCookieHttpOnly;
    private Boolean isCookieSecure;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getQueueDomain() {
        return queueDomain;
    }

    public void setQueueDomain(String queueDomain) {
        this.queueDomain = queueDomain;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public CancelEventConfig() {
        this.version = -1;
    }

    public String getActionName() {
        return actionName;
    }

    public Boolean getIsCookieHttpOnly() {
        return this.isCookieHttpOnly;
    }

    public void setIsCookieHttpOnly(Boolean isCookieHttpOnly) {
        this.isCookieHttpOnly = isCookieHttpOnly;
    }

    public Boolean getIsCookieSecure() {
        return this.isCookieSecure;
    }

    public void setIsCookieSecure(Boolean cookieSecure) {
        this.isCookieSecure = cookieSecure;
    }

    @Override
    public String toString() {
        return "EventId:" + eventId
                + "&Version:" + version
                + "&QueueDomain:" + queueDomain
                + "&CookieDomain:" + cookieDomain
                + "&IsCookieHttpOnly:" + isCookieHttpOnly
                + "&IsCookieSecure:" + isCookieSecure
                + "&ActionName:" + actionName;
    }
}
