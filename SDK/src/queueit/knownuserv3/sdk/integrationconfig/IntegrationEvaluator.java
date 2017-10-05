package queueit.knownuserv3.sdk.integrationconfig;

import javax.servlet.http.Cookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import javax.servlet.http.HttpServlet;
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
        
        if(request == null)
           throw new Exception("request is null");
        
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
        switch (triggerPart.ValidatorType) {
            case ValidatorType.URL_VALIDATOR:
                return UrlValidatorHelper.evaluate(triggerPart, currentPageUrl);
            case ValidatorType.COOKIE_VALIDATOR:
                return CookieValidatorHelper.evaluate(triggerPart, request.getCookies());
            case ValidatorType.USERAGENT_VALIDATOR:
                return UserAgentValidatorHelper.evaluate(triggerPart, request.getHeader("User-Agent"));
            case ValidatorType.HTTPHEADER_VALIDATOR:
                return HttpHeaderValidatorHelper.evaluate(triggerPart, request);
            default:
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
                triggerPart.ValueToCompare);
    }

    private static String getUrlPart(TriggerPart triggerPart, String url) {
        switch (triggerPart.UrlPart) {
            case UrlPartType.PAGE_PATH:
                return getPathFromUrl(url);
            case UrlPartType.PAGE_URL:
                return url;
            case UrlPartType.HOST_NAME:
                return getHostNameFromUrl(url);
            default:
                return "";
        }
    }

    private static String getHostNameFromUrl(String url) {
        return getMatchFromUrl(url,
                "^(([^:/\\?#]+):)?(//(?<hostname>[^/\\?#]*))?([^\\?#]*)(\\?([^#]*))?(#(.*))?",
                "hostname");
    }

    private static String getPathFromUrl(String url) {
        return getMatchFromUrl(url,
                "^(([^:/\\?#]+):)?(//([^/\\?#]*))?(?<path>[^\\?#]*)(\\?([^#]*))?(#(.*))?",
                "path");
    }

    private static String getMatchFromUrl(String url, String urlMatcher, String matchName) {
        Pattern pattern = Pattern.compile(urlMatcher);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            return "";
        }
        
        return matcher.group(matchName);        
    }
}

final class CookieValidatorHelper {

    public static boolean evaluate(TriggerPart triggerPart, Cookie[] cookieCollection) {
        return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                getCookie(triggerPart.CookieName, cookieCollection),
                triggerPart.ValueToCompare);
    }

    private static String getCookie(String cookieName, Cookie[] cookieCollection) {
        if(cookieCollection == null)
            return "";
        
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
                triggerPart.ValueToCompare);
    }
}

    final class HttpHeaderValidatorHelper {
        public static boolean evaluate(TriggerPart triggerPart, HttpServletRequest request)
        {
            return ComparisonOperatorHelper.evaluate(triggerPart.Operator,
                triggerPart.IsNegative,
                triggerPart.IsIgnoreCase,
                request.getHeader(triggerPart.HttpHeaderName),
                triggerPart.ValueToCompare);
        }
    }

final class ComparisonOperatorHelper {

    public static boolean evaluate(String opt, boolean isNegative, boolean isIgnoreCase, String left, String right) {
        left = (left != null) ? left : "";
        right = (right != null) ? right : "";
        switch (opt) {
            case ComparisonOperatorType.EQUALS:
                return equals(left, right, isNegative, isIgnoreCase);
            case ComparisonOperatorType.CONTAINS:
                return contains(left, right, isNegative, isIgnoreCase);
            case ComparisonOperatorType.STARTS_WITH:
                return startsWith(left, right, isNegative, isIgnoreCase);
            case ComparisonOperatorType.ENDS_WITH:
                return endsWith(left, right, isNegative, isIgnoreCase);
            case ComparisonOperatorType.MATCHES_WITH:
                return matchesWith(left, right, isNegative, isIgnoreCase);
            default:
                return false;
        }
    }

    private static boolean contains(String left, String right, boolean isNegative, boolean ignoreCase) {
        if (right.equals("*")) {
            return true;
        }
        boolean evaluation;
        if (ignoreCase) {
            evaluation = left.toUpperCase().contains(right.toUpperCase());
        } else {
            evaluation = left.contains(right);
        }
        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean equals(String left, String right, boolean isNegative, boolean ignoreCase) {
        boolean evaluation;

        if (ignoreCase) {
            evaluation = left.toUpperCase().equals(right.toUpperCase());
        } else {
            evaluation = left.equals(right);
        }

        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean endsWith(String left, String right, boolean isNegative, boolean ignoreCase) {
        boolean evaluation;

        if (ignoreCase) {
            evaluation = left.toUpperCase().endsWith(right.toUpperCase());
        } else {
            evaluation = left.endsWith(right);
        }

        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean startsWith(String left, String right, boolean isNegative, boolean ignoreCase) {
        boolean evaluation;

        if (ignoreCase) {
            evaluation = left.toUpperCase().startsWith(right.toUpperCase());
        } else {
            evaluation = left.startsWith(right);
        }

        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }

    private static boolean matchesWith(String left, String right, boolean isNegative, boolean isIgnoreCase) {
        Pattern pattern;
        if (isIgnoreCase) {
            pattern = Pattern.compile(right, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile(right);
        }

        boolean evaluation = pattern.matcher(left).matches();
        if (isNegative) {
            return !evaluation;
        } else {
            return evaluation;
        }
    }
}
