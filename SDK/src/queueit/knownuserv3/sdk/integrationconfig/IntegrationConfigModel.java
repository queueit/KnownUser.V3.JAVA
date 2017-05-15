package queueit.knownuserv3.sdk.integrationconfig;

public class IntegrationConfigModel {

    public String Name;
    public String EventId;
    public String CookieDomain;
    public String LayoutName;
    public String Culture;
    public boolean ExtendCookieValidity;
    public int CookieValidityMinute;
    public String QueueDomain;
    public String RedirectLogic;
    public String ForcedTargetUrl;
    public TriggerModel[] Triggers;
}
