package queueit.knownuserv3.sdk;

public class CancelEventConfig {

    private String eventId;
    private String queueDomain;
    private String cookieDomain;
    private int version;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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
    
    @Override
    public String toString() {
        return "EventId:" + eventId + 
               "&Version:" + version +
               "&QueueDomain:" + queueDomain + 
               "&CookieDomain:" + cookieDomain;
    }
}