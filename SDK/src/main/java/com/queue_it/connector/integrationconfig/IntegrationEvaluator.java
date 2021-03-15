package com.queue_it.connector.integrationconfig;


import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

interface IIntegrationEvaluator {

    IntegrationConfigModel getMatchedIntegrationConfig(
            CustomerIntegration customerIntegration,
            String currentPageUrl,
            HttpServletRequest request) throws Exception;
}

public class IntegrationEvaluator implements IIntegrationEvaluator {

    @Override
    public IntegrationConfigModel getMatchedIntegrationConfig(
            CustomerIntegration customerIntegration,
            String currentPageUrl,
            HttpServletRequest request) throws Exception {

        if (request == null) {
            throw new Exception("request is null");
        }

        for (IntegrationConfigModel integration : customerIntegration.Integrations) {
            for (TriggerModel trigger : integration.Triggers) {
                if (evaluateTrigger(trigger, currentPageUrl, request)) {
                    return integration;
                }
            }
        }
        return null;
    }

    private boolean evaluateTrigger(
            TriggerModel trigger,
            String currentPageUrl,
            HttpServletRequest request) {
        if (trigger.LogicalOperator.equals(LogicalOperatorType.OR)) {
            for (TriggerPart part : trigger.TriggerParts) {
                if (evaluateTriggerPart(part, currentPageUrl, request)) {
                    return true;
                }
            }
            return false;
        } else {
            for (TriggerPart part : trigger.TriggerParts) {
                if (!evaluateTriggerPart(part, currentPageUrl, request)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean evaluateTriggerPart(TriggerPart triggerPart, String currentPageUrl, HttpServletRequest request) {
        if (ValidatorType.URL_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return UrlValidatorHelper.evaluate(triggerPart, currentPageUrl);
        } else if (ValidatorType.COOKIE_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return CookieValidatorHelper.evaluate(triggerPart, request.getCookies());
        } else if (ValidatorType.USERAGENT_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return UserAgentValidatorHelper.evaluate(triggerPart, request.getHeader("User-Agent"));
        } else if (ValidatorType.HTTPHEADER_VALIDATOR.equals(triggerPart.ValidatorType)) {
            return HttpHeaderValidatorHelper.evaluate(triggerPart, request);
        } else {
            return false;
        }
    }
}

final class UrlValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, String url) {
        return ComparisonOperatorHelper.evaluate(
                triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                getUrlPart(triggerPart, url),
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }

    private static String getUrlPart(TriggerPart triggerPart, String url) {
        if (UrlPartType.PAGE_URL.equals(triggerPart.UrlPart)) {
            return url;
        }

        try {
            URL oUrl = new URL(url);

            if (UrlPartType.PAGE_PATH.equals(triggerPart.UrlPart)) {
                return oUrl.getPath();
            } else if (UrlPartType.HOST_NAME.equals(triggerPart.UrlPart)) {
                return oUrl.getHost();
            } else {
                return "";
            }
        } catch (MalformedURLException ex) {
            return "";
        }
    }
}

final class CookieValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, Cookie[] cookieCollection) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                getCookie(triggerPart.CookieName, cookieCollection),
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }

    private static String getCookie(String cookieName, Cookie[] cookieCollection) {
        if (cookieCollection == null) {
            return "";
        }

        for (Cookie cookie : cookieCollection) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return "";
    }
}

final class UserAgentValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, String userAgent) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                userAgent,
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }
}

final class HttpHeaderValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, HttpServletRequest request) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                request.getHeader(triggerPart.HttpHeaderName),
                triggerPart.ValueToCompare,
                triggerPart.ValuesToCompare);
    }
}

final class ComparisonOperatorHelper {

    public static boolean evaluate(
            String opt,
            boolean isNegative,
            boolean isIgnoreCase,
            String value,
            String valueToCompare,
            String[] valuesToCompare) {

        value = (value != null) ? value : "";
        valueToCompare = (valueToCompare != null) ? valueToCompare : "";
        valuesToCompare = (valuesToCompare != null) ? valuesToCompare : new String[0];

        if (ComparisonOperatorType.EQUALS.equals(opt)) {
            return equals(value, valueToCompare, isNegative, isIgnoreCase);
        } else if (ComparisonOperatorType.CONTAINS.equals(opt)) {
            return contains(value, valueToCompare, isNegative, isIgnoreCase);
        }
        else if (ComparisonOperatorType.EQUALS_ANY.equals(opt)) {
            return equalsAny(value, valuesToCompare, isNegative, isIgnoreCase);
        } else if (ComparisonOperatorType.CONTAINS_ANY.equals(opt)) {
            return containsAny(value, valuesToCompare, isNegative, isIgnoreCase);
        } else {
            return false;
        }
    }

    private static boolean contains(String value, String valueToCompare, boolean isNegative, boolean ignoreCase) {
        if (valueToCompare.equals("*") && value != null && !value.isEmpty()) {
            return true;
        }
        boolean evaluation;
        if (ignoreCase) {
            evaluation = value.toUpperCase().contains(valueToCompare.toUpperCase());
        } else {
            evaluation = value.contains(valueToCompare);
        }
        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean equals(String value, String valueToCompare, boolean isNegative, boolean ignoreCase) {
        boolean evaluation;

        if (ignoreCase) {
            evaluation = value.toUpperCase().equals(valueToCompare.toUpperCase());
        } else {
            evaluation = value.equals(valueToCompare);
        }

        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean equalsAny(String value, String[] valuesToCompare, boolean isNegative, boolean isIgnoreCase) {
        for (String valueToCompare : valuesToCompare) {
            if (equals(value, valueToCompare, false, isIgnoreCase)) {
                return !isNegative;
            }
        }
        return isNegative;
    }

    private static boolean containsAny(String value, String[] valuesToCompare, boolean isNegative, boolean isIgnoreCase) {
        for (String valueToCompare : valuesToCompare) {
            if (contains(value, valueToCompare, false, isIgnoreCase)) {
                return !isNegative;
            }
        }
        return isNegative;
    }
}
