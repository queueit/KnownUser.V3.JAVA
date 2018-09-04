package queueit.knownuserv3.sdk.integrationconfig;

public class TriggerPart {

    public String ValidatorType;
    public String Operator;
    public String ValueToCompare;
    public String[] ValuesToCompare;
    public boolean IsNegative;
    public boolean IsIgnoreCase;
    //UrlValidator
    public String UrlPart;
    //CookieValidator
    public String CookieName;
    //HttpHeaderValidator
    public String HttpHeaderName;
}