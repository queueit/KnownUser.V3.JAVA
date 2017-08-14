package queueit.knownuserv3.sdk.integrationconfig;

final class ValidatorType {

    public static final String URL_VALIDATOR = "UrlValidator";
    public static final String COOKIE_VALIDATOR = "CookieValidator";
    public static final String USERAGENT_VALIDATOR = "UserAgentValidator";
}

final class UrlPartType {

    public static final String HOST_NAME = "HostName";
    public static final String PAGE_PATH = "PagePath";
    public static final String PAGE_URL = "PageUrl";
}

final class ComparisonOperatorType {

    public static final String EQUALS = "Equals";
    public static final String CONTAINS = "Contains";
    public static final String STARTS_WITH = "StartsWith";
    public static final String ENDS_WITH = "EndsWith";
    public static final String MATCHES_WITH = "MatchesWith";
}

final class LogicalOperatorType {

    public static final String OR = "Or";
    public static final String AND = "And";
}
