package com.queue_it.connector.integrationconfig;

final class ValidatorType {

    public static final String URL_VALIDATOR = "UrlValidator";
    public static final String COOKIE_VALIDATOR = "CookieValidator";
    public static final String USERAGENT_VALIDATOR = "UserAgentValidator";
    public static final String HTTPHEADER_VALIDATOR = "HttpHeaderValidator";
}

final class UrlPartType {

    public static final String HOST_NAME = "HostName";
    public static final String PAGE_PATH = "PagePath";
    public static final String PAGE_URL = "PageUrl";
}

final class ComparisonOperatorType {

    public static final String EQUALS = "Equals";
    public static final String CONTAINS = "Contains";
    public static final String EQUALS_ANY = "EqualsAny";
    public static final String CONTAINS_ANY = "ContainsAny";
}

final class LogicalOperatorType {

    public static final String OR = "Or";
    public static final String AND = "And";
}
