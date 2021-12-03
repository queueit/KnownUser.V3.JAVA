package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.queue_it.connector.integrationconfig.*;

public class KnownUser {

    public static final String QueueITTokenKey = "queueittoken";
    public static final String QueueITDebugKey = "queueitdebug";
    public static final String QueueITAjaxHeaderKey = "x-queueit-ajaxpageurl";
    private static IUserInQueueService _userInQueueService;

    private static IUserInQueueService getUserInQueueService(HttpServletRequest request, HttpServletResponse response) {

        if (_userInQueueService == null) {
            return new UserInQueueService(new UserInQueueStateCookieRepository(new CookieManager(request, response)));
        }
        return _userInQueueService;
    }

    /**
     * Use for supplying explicit mock for testing purpose.
     */
    static void setUserInQueueService(IUserInQueueService mockUserInQueueService) {
        _userInQueueService = mockUserInQueueService;
    }

    public static RequestValidationResult validateRequestByIntegrationConfig(String currentUrlWithoutQueueITToken,
            String queueitToken, CustomerIntegration customerIntegrationInfo, String customerId,
            KnownUserRequestWrapper request, HttpServletResponse response, String secretKey) throws Exception {

        Map<String, String> debugEntries = new HashMap<String, String>();

        ConnectorDiagnostics connectorDiagnostics = ConnectorDiagnostics.Verify(customerId, secretKey, queueitToken);

        if (connectorDiagnostics.hasError) {
            return connectorDiagnostics.validationResult;
        }

        try {
            if (connectorDiagnostics.isEnabled) {
                debugEntries.put("SdkVersion", UserInQueueService.SDK_VERSION);
                debugEntries.put("Runtime", GetRuntime());
                String conVer = (customerIntegrationInfo != null) ? Integer.toString(customerIntegrationInfo.Version) : "NULL";
                debugEntries.put("ConfigVersion", conVer);
                debugEntries.put("PureUrl", currentUrlWithoutQueueITToken);
                debugEntries.put("QueueitToken", queueitToken);
                debugEntries.put("OriginalUrl", getOriginalUrl(request));

                logMoreRequestDetails(debugEntries, request);
            }

            if (Utils.isNullOrWhiteSpace(currentUrlWithoutQueueITToken)) {
                throw new Exception("currentUrlWithoutQueueITToken can not be null or empty.");
            }

            if (customerIntegrationInfo == null) {
                throw new KnowUserException("customerIntegrationInfo can not be null.");
            }

            IntegrationEvaluator configEvaluater = new IntegrationEvaluator();

            IntegrationConfigModel matchedConfig = configEvaluater.getMatchedIntegrationConfig(customerIntegrationInfo,
                    currentUrlWithoutQueueITToken, request);

            if (connectorDiagnostics.isEnabled) {
                String matchedConf = (matchedConfig != null) ? matchedConfig.Name : "NULL";
                debugEntries.put("MatchedConfig", matchedConf);
            }

            if (matchedConfig == null) {
                return new RequestValidationResult(null, null, null, null, null, null);
            }

            // unspecified or 'Queue' specified
            if (Utils.isNullOrWhiteSpace(matchedConfig.ActionType)
                    || ActionType.QUEUE_ACTION.equals(matchedConfig.ActionType)) {
                return handleQueueAction(matchedConfig, currentUrlWithoutQueueITToken, customerIntegrationInfo,
                        queueitToken, customerId, request, response, secretKey, debugEntries, connectorDiagnostics.isEnabled);

            } else if (ActionType.CANCEL_ACTION.equals(matchedConfig.ActionType)) {
                return handleCancelAction(matchedConfig, customerIntegrationInfo, currentUrlWithoutQueueITToken,
                        queueitToken, customerId, request, response, secretKey, debugEntries, connectorDiagnostics.isEnabled);
            }

            // for all unknown types default to 'Ignore'
            else {
                return handleIgnoreAction(request, response, matchedConfig.Name);
            }
        }
        catch (Exception e) {
            if (connectorDiagnostics.isEnabled) {
                debugEntries.put("Exception", e.getMessage());
            }
            throw e;
        }
        finally {
            setDebugCookie(debugEntries, request, response);
        }
    }

    public static RequestValidationResult cancelRequestByLocalConfig(String targetUrl, String queueitToken,
            CancelEventConfig cancelConfig, String customerId, HttpServletRequest request, HttpServletResponse response,
            String secretKey) throws Exception {

        Map<String, String> debugEntries = new HashMap<String, String>();

        ConnectorDiagnostics connectorDiagnostics = ConnectorDiagnostics.Verify(customerId, secretKey, queueitToken);

        if (connectorDiagnostics.hasError) {
            return connectorDiagnostics.validationResult;
        }

        try {
            return cancelRequestByLocalConfig(targetUrl, queueitToken, cancelConfig, customerId, request, response,
                    secretKey, debugEntries, connectorDiagnostics.isEnabled);
        }
        catch (Exception e) {
            if(connectorDiagnostics.isEnabled) {
                debugEntries.put("Exception", e.getMessage());
            }
            throw e;
        }
        finally {
            setDebugCookie(debugEntries, request, response);
        }
    }

    private static RequestValidationResult cancelRequestByLocalConfig(String targetUrl, String queueitToken,
            CancelEventConfig cancelConfig, String customerId, HttpServletRequest request, HttpServletResponse response,
            String secretKey, Map<String, String> debugEntries, boolean isDebug) throws Exception {

        targetUrl = generateTargetUrl(targetUrl, request);

        if (isDebug) {
            debugEntries.put("SdkVersion", UserInQueueService.SDK_VERSION);
            debugEntries.put("Runtime", GetRuntime());
            debugEntries.put("TargetUrl", targetUrl);
            debugEntries.put("QueueitToken", queueitToken);
            debugEntries.put("CancelConfig", (cancelConfig != null) ? cancelConfig.toString() : "NULL");
            debugEntries.put("OriginalUrl", getOriginalUrl(request));
            logMoreRequestDetails(debugEntries, request);
        }

        if (Utils.isNullOrWhiteSpace(targetUrl)) {
            throw new Exception("targetUrl can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(customerId)) {
            throw new Exception("customerId can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new Exception("secretKey can not be null or empty.");
        }
        if (cancelConfig == null) {
            throw new Exception("cancelConfig can not be null.");
        }
        if (Utils.isNullOrWhiteSpace(cancelConfig.getEventId())) {
            throw new Exception("EventId from cancelConfig can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(cancelConfig.getQueueDomain())) {
            throw new Exception("QueueDomain from cancelConfig can not be null or empty.");
        }

        IUserInQueueService userInQueueService = getUserInQueueService(request, response);
        RequestValidationResult result = userInQueueService.validateCancelRequest(targetUrl, cancelConfig, customerId,
                secretKey);
        result.isAjaxResult = isQueueAjaxCall(request);

        return result;
    }

    public static RequestValidationResult resolveQueueRequestByLocalConfig(
            String targetUrl, String queueitToken,
            QueueEventConfig queueConfig, String customerId, HttpServletRequest request, HttpServletResponse response,
            String secretKey) throws Exception {

        ConnectorDiagnostics connectorDiagnostics = ConnectorDiagnostics.Verify(customerId, secretKey, queueitToken);

        if (connectorDiagnostics.hasError) {
            return connectorDiagnostics.validationResult;
        }

        Map<String, String> debugEntries = new HashMap<String, String>();

        try {
            targetUrl = generateTargetUrl(targetUrl, request);

            return resolveQueueRequestByLocalConfig(targetUrl, queueitToken, queueConfig, customerId, request, response,
                    secretKey, debugEntries, connectorDiagnostics.isEnabled);
        }
        catch (Exception e) {
            if(connectorDiagnostics.isEnabled) {
                debugEntries.put("Exception", e.getMessage());
            }
            throw e;
        }
        finally {
            setDebugCookie(debugEntries, request, response);
        }
    }

    private static RequestValidationResult resolveQueueRequestByLocalConfig(String targetUrl, String queueitToken,
            QueueEventConfig queueConfig, String customerId, HttpServletRequest request, HttpServletResponse response,
            String secretKey, Map<String, String> debugEntries, boolean isDebug) throws Exception {

        if (isDebug) {
            debugEntries.put("SdkVersion", UserInQueueService.SDK_VERSION);
            debugEntries.put("Runtime", GetRuntime());
            debugEntries.put("TargetUrl", targetUrl);
            debugEntries.put("QueueitToken", queueitToken);
            debugEntries.put("QueueConfig", (queueConfig != null) ? queueConfig.toString() : "NULL");
            debugEntries.put("OriginalUrl", getOriginalUrl(request));

            logMoreRequestDetails(debugEntries, request);
        }

        if (Utils.isNullOrWhiteSpace(customerId)) {
            throw new Exception("customerId can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new Exception("secretKey can not be null or empty.");
        }
        if (queueConfig == null) {
            throw new Exception("eventConfig can not be null.");
        }
        if (Utils.isNullOrWhiteSpace(queueConfig.getEventId())) {
            throw new Exception("EventId from queueConfig can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(queueConfig.getQueueDomain())) {
            throw new Exception("QueueDomain from queueConfig can not be null or empty.");
        }
        if (queueConfig.getCookieValidityMinute() <= 0) {
            throw new Exception("cookieValidityMinute from queueConfig should be greater than 0.");
        }
        if (queueitToken == null) {
            queueitToken = "";
        }

        IUserInQueueService userInQueueService = getUserInQueueService(request, response);
        RequestValidationResult result = userInQueueService.validateQueueRequest(targetUrl, queueitToken, queueConfig,
                customerId, secretKey);
        result.isAjaxResult = isQueueAjaxCall(request);

        return result;
    }

    public static void extendQueueCookie(String eventId,
                                         int cookieValidityMinute,
                                         String cookieDomain,
                                         Boolean isCookieHttpOnly,
                                         Boolean isCookieSecure,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         String secretKey) throws Exception {

        if (Utils.isNullOrWhiteSpace(eventId)) {
            throw new Exception("eventId can not be null or empty.");
        }
        if (cookieValidityMinute <= 0) {
            throw new Exception("cookieValidityMinute should be greater than 0.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new Exception("secretKey can not be null or empty.");
        }

        IUserInQueueService userInQueueService = getUserInQueueService(request, response);
        userInQueueService.extendQueueCookie(eventId, cookieValidityMinute, cookieDomain, isCookieHttpOnly, isCookieSecure, secretKey);
    }

    private static void setDebugCookie(Map<String, String> debugEntries, HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {

        if (debugEntries.isEmpty()) {
            return;
        }

        ICookieManager cookieManager = new CookieManager(request, response);
        String cookieValue = "";
        for (Map.Entry<String, String> entry : debugEntries.entrySet()) {
            cookieValue += (entry.getKey() + "=" + entry.getValue() + "|");
        }
        if (!"".equals(cookieValue)) {
            cookieValue = cookieValue.substring(0, cookieValue.length() - 1); // remove trailing char
        }
        cookieManager.setCookie(QueueITDebugKey, cookieValue, null, null, false, false);
    }

    private static void logMoreRequestDetails(Map<String, String> debugEntries, HttpServletRequest request) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        debugEntries.put("ServerUtcTime", nowAsISO);
        debugEntries.put("RequestIP", request.getRemoteAddr());
        debugEntries.put("RequestHttpHeader_Via", request.getHeader("via") != null ? request.getHeader("via") : "");
        debugEntries.put("RequestHttpHeader_Forwarded",
                request.getHeader("forwarded") != null ? request.getHeader("forwarded") : "");
        debugEntries.put("RequestHttpHeader_XForwardedFor",
                request.getHeader("x-forwarded-for") != null ? request.getHeader("x-forwarded-for") : "");
        debugEntries.put("RequestHttpHeader_XForwardedHost",
                request.getHeader("x-forwarded-host") != null ? request.getHeader("x-forwarded-host") : "");
        debugEntries.put("RequestHttpHeader_XForwardedProto",
                request.getHeader("x-forwarded-proto") != null ? request.getHeader("x-forwarded-proto") : "");
    }

    private static String getOriginalUrl(HttpServletRequest request) {
        return (request.getQueryString() != null) ? request.getRequestURL().toString() + "?" + request.getQueryString()
                : request.getRequestURL().toString();
    }

    private static RequestValidationResult handleQueueAction(IntegrationConfigModel matchedConfig,
            String currentUrlWithoutQueueITToken, CustomerIntegration customerIntegrationInfo, String queueitToken,
            String customerId, HttpServletRequest request, HttpServletResponse response, String secretKey,
            Map<String, String> debugEntries, boolean isDebug) throws Exception {
        String targetUrl;

        if ("ForecedTargetUrl".equals(matchedConfig.RedirectLogic) || // support for typo
                "ForcedTargetUrl".equals(matchedConfig.RedirectLogic)) {
            targetUrl = matchedConfig.ForcedTargetUrl;
        } else if ("EventTargetUrl".equals(matchedConfig.RedirectLogic)) {
            targetUrl = "";
        } else {
            targetUrl = generateTargetUrl(currentUrlWithoutQueueITToken, request);
        }

        QueueEventConfig queueConfig = new QueueEventConfig();
        queueConfig.setQueueDomain(matchedConfig.QueueDomain);
        queueConfig.setCulture(matchedConfig.Culture);
        queueConfig.setEventId(matchedConfig.EventId);
        queueConfig.setExtendCookieValidity(matchedConfig.ExtendCookieValidity);
        queueConfig.setLayoutName(matchedConfig.LayoutName);
        queueConfig.setCookieValidityMinute(matchedConfig.CookieValidityMinute);
        queueConfig.setCookieDomain(matchedConfig.CookieDomain);
        queueConfig.setIsCookieHttpOnly(matchedConfig.IsCookieHttpOnly);
        queueConfig.setIsCookieSecure(matchedConfig.IsCookieSecure);
        queueConfig.setVersion(customerIntegrationInfo.Version);
        queueConfig.setActionName(matchedConfig.Name);

        return resolveQueueRequestByLocalConfig(targetUrl, queueitToken, queueConfig, customerId, request, response,
                secretKey, debugEntries, isDebug);
    }

    private static RequestValidationResult handleCancelAction(IntegrationConfigModel matchedConfig,
            CustomerIntegration customerIntegrationInfo, String currentUrlWithoutQueueITToken, String queueitToken,
            String customerId, HttpServletRequest request, HttpServletResponse response, String secretKey,
            Map<String, String> debugEntries, boolean isDebug) throws Exception {

        CancelEventConfig cancelConfig = new CancelEventConfig();
        cancelConfig.setQueueDomain(matchedConfig.QueueDomain);
        cancelConfig.setEventId(matchedConfig.EventId);
        cancelConfig.setCookieDomain(matchedConfig.CookieDomain);
        cancelConfig.setIsCookieHttpOnly(matchedConfig.IsCookieHttpOnly);
        cancelConfig.setIsCookieSecure(matchedConfig.IsCookieSecure);
        cancelConfig.setVersion(customerIntegrationInfo.Version);
        cancelConfig.setActionName(matchedConfig.Name);

        String targetUrl = generateTargetUrl(currentUrlWithoutQueueITToken, request);

        return cancelRequestByLocalConfig(targetUrl, queueitToken, cancelConfig, customerId, request, response,
                secretKey, debugEntries, isDebug);
    }

    private static RequestValidationResult handleIgnoreAction(HttpServletRequest request, HttpServletResponse response,
            String actionName) {

        IUserInQueueService userInQueueService = getUserInQueueService(request, response);
        RequestValidationResult result = userInQueueService.getIgnoreActionResult(actionName);
        result.isAjaxResult = isQueueAjaxCall(request);
        return result;
    }

    private static String generateTargetUrl(String originalTargetUrl, HttpServletRequest request) {
        try {
            return !isQueueAjaxCall(request) ? originalTargetUrl
                    : URLDecoder.decode(request.getHeader(QueueITAjaxHeaderKey), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    private static boolean isQueueAjaxCall(HttpServletRequest request) {
        return !Utils.isNullOrWhiteSpace(request.getHeader(QueueITAjaxHeaderKey));
    }

    public static String GetRuntime() {
        try {
            return System.getProperty("java.runtime.version");
        } catch (Exception ex) {
            return "unknown";
        }
    }
}

interface ICookieManager {

    void setCookie(String name, String value, Integer expiration, String domain, Boolean isHttpOnly, Boolean isSecure);

    String getCookie(String name);
}

class CookieManager implements ICookieManager {

    HttpServletRequest request;
    HttpServletResponse response;

    public CookieManager(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public void setCookie(String name, String value, Integer expiration, String domain, Boolean isHttpOnly, Boolean isSecure) {
        if (response == null) {
            return;
        }

        Cookie cookie = new Cookie(name, value);
        if (value == null) {
            value = "";
        }
        try {
            cookie.setValue(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }
        if (expiration != null) {
            cookie.setMaxAge(expiration);
        }
        cookie.setPath("/");
        if (!Utils.isNullOrWhiteSpace(domain)) {
            cookie.setDomain(domain);
        }

        cookie.setHttpOnly(Boolean.TRUE.equals(isHttpOnly));
        cookie.setSecure(Boolean.TRUE.equals(isSecure));

        response.addCookie(cookie);
    }

    @Override
    public String getCookie(String name) {
        if (request == null) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                try {
                    return URLDecoder.decode(cookie.getValue(), "UTF-8");
                } catch (Exception ex) {
                }
            }
        }
        return null;
    }
}
