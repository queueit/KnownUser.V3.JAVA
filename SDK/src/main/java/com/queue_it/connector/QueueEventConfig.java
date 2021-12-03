package com.queue_it.connector;

public class QueueEventConfig {

    private String eventId;
    private String layoutName;
    private String culture;
    private String queueDomain;
    private boolean extendCookieValidity;
    private int cookieValidityMinute;
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

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getQueueDomain() {
        return queueDomain;
    }

    public void setQueueDomain(String queueDomain) {
        this.queueDomain = queueDomain;
    }

    public boolean getExtendCookieValidity() {
        return extendCookieValidity;
    }

    public void setExtendCookieValidity(boolean extendCookieValidity) {
        this.extendCookieValidity = extendCookieValidity;
    }

    public int getCookieValidityMinute() {
        return cookieValidityMinute;
    }

    public void setCookieValidityMinute(int cookieValidityMinute) {
        this.cookieValidityMinute = cookieValidityMinute;
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

    public QueueEventConfig() {
        this.version = -1;
    }

    public String getActionName() {
        return actionName;
    }

    public Boolean getIsCookieHttpOnly() {
        return this.isCookieHttpOnly;
    }

    public void setIsCookieHttpOnly(Boolean cookieHttpOnly) {
        this.isCookieHttpOnly = cookieHttpOnly;
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
                + "&ExtendCookieValidity:" + extendCookieValidity
                + "&CookieValidityMinute:" + cookieValidityMinute
                + "&LayoutName:" + layoutName
                + "&Culture:" + culture
                + "&ActionName:" + actionName;
    }
}
