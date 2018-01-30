package queueit.knownuserv3.sdk;

public class QueueEventConfig {

    private String eventId;
    private String layoutName;
    private String culture;
    private String queueDomain;
    private boolean extendCookieValidity;
    private int cookieValidityMinute;
    private String cookieDomain;
    private int version;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    @Override
    public String toString() {
        return "EventId:" + eventId
                + "&Version:" + version
                + "&QueueDomain:" + queueDomain
                + "&CookieDomain:" + cookieDomain
                + "&ExtendCookieValidity:" + extendCookieValidity
                + "&CookieValidityMinute:" + cookieValidityMinute
                + "&LayoutName:" + layoutName
                + "&Culture:" + culture;
    }
}
