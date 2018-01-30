package queueit.knownuserv3.sdk;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import queueit.knownuserv3.sdk.integrationconfig.*;

public class KnownUser {
    
    public static final String QueueITTokenKey = "queueittoken";
    public static final String QueueITDebugKey = "queueitdebug";
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
            String queueitToken, CustomerIntegration customerIntegrationInfo,
            String customerId, HttpServletRequest request,
            HttpServletResponse response, String secretKey) throws Exception {

        Map<String, String> debugEntries = new HashMap<>();
        
        try {
            boolean isDebug = getIsDebug(queueitToken, secretKey);
            if (isDebug) {
                debugEntries.put("ConfigVersion", Integer.toString(customerIntegrationInfo.Version));
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

            Cookie[] cookies = request != null ? request.getCookies() : new Cookie[0];        

            IntegrationEvaluator configEvaluater = new IntegrationEvaluator();

            IntegrationConfigModel matchedConfig = configEvaluater.getMatchedIntegrationConfig(
                    customerIntegrationInfo, currentUrlWithoutQueueITToken, request);

            if (isDebug) {
                String matchedConfigName = (matchedConfig != null) ? matchedConfig.Name : "NULL";
                debugEntries.put("MatchedConfig", matchedConfigName);                
            }

            if (matchedConfig == null) {
                return new RequestValidationResult(null, null, null, null, null);
            }

            // unspecified or 'Queue' specified
            if(Utils.isNullOrWhiteSpace(matchedConfig.ActionType) || ActionType.QUEUE_ACTION.equals(matchedConfig.ActionType)) {
                return handleQueueAction(matchedConfig, currentUrlWithoutQueueITToken, customerIntegrationInfo, queueitToken, customerId, request, response, secretKey, debugEntries);
            }
            else if (ActionType.CANCEL_ACTION.equals(matchedConfig.ActionType)){
                return handleCancelAction(matchedConfig, customerIntegrationInfo, currentUrlWithoutQueueITToken, queueitToken, customerId, request, response, secretKey, debugEntries);
            }            
            // for all unknown types default to 'Ignore'
            else {
                return handleIgnoreAction(request, response);
            }
        }
        finally {
            setDebugCookie(debugEntries, request, response);
        }
    }

    public static RequestValidationResult cancelRequestByLocalConfig(
            String targetUrl, String queueitToken, CancelEventConfig cancelConfig,
            String customerId, HttpServletRequest request,
            HttpServletResponse response, String secretKey) throws Exception {
        
        Map<String, String> debugEntries = new HashMap<>();
        
        try {
            return cancelRequestByLocalConfig(
                    targetUrl, queueitToken, cancelConfig, customerId, request, response, secretKey, debugEntries);
        }
        finally {
            setDebugCookie(debugEntries, request, response);
        }
    }
    
    private static RequestValidationResult cancelRequestByLocalConfig(
            String targetUrl, String queueitToken, CancelEventConfig cancelConfig,
            String customerId, HttpServletRequest request,
            HttpServletResponse response, String secretKey, 
            Map<String, String> debugEntries) throws Exception {
        
        boolean isDebug = getIsDebug(queueitToken, secretKey);
        if (isDebug) {
            debugEntries.put("TargetUrl", targetUrl);
            debugEntries.put("QueueitToken", queueitToken);
            debugEntries.put("CancelConfig", cancelConfig != null ? cancelConfig.toString() : "NULL");
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
        return userInQueueService.validateCancelRequest(targetUrl, cancelConfig, customerId, secretKey);
    }
    
    public static RequestValidationResult resolveQueueRequestByLocalConfig(
            String targetUrl, String queueitToken, QueueEventConfig queueConfig,
            String customerId, HttpServletRequest request,
            HttpServletResponse response, String secretKey) throws Exception {
        
        Map<String, String> debugEntries = new HashMap<>();
        
        try {
            return resolveQueueRequestByLocalConfig(
                    targetUrl, queueitToken, queueConfig, customerId, request, response, secretKey, debugEntries);
        }
        finally {
            setDebugCookie(debugEntries, request, response);
        }
    }
    
    private static RequestValidationResult resolveQueueRequestByLocalConfig(
            String targetUrl, String queueitToken, QueueEventConfig queueConfig,
            String customerId, HttpServletRequest request,
            HttpServletResponse response, String secretKey, 
            Map<String, String> debugEntries) throws Exception {
        
        boolean isDebug = getIsDebug(queueitToken, secretKey);
        if (isDebug) {
            debugEntries.put("TargetUrl", targetUrl);
            debugEntries.put("QueueitToken", queueitToken);
            debugEntries.put("QueueConfig", queueConfig != null ? queueConfig.toString() : "NULL");
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
        return userInQueueService.validateQueueRequest(targetUrl, queueitToken, queueConfig, customerId, secretKey);
    }

    public static void extendQueueCookie(String eventId,
            int cookieValidityMinute,
            String cookieDomain,
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
        userInQueueService.extendQueueCookie(eventId, cookieValidityMinute, cookieDomain, secretKey);
    }
    
    private static void setDebugCookie(Map<String, String> debugEntries, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        if(debugEntries.isEmpty())
            return;
        
        ICookieManager cookieManager = new CookieManager(request, response);
        String cookieValue = "";
        for (Map.Entry<String, String> entry : debugEntries.entrySet()) {
            cookieValue += (entry.getKey() + "=" + entry.getValue() + "|");            
        }
        if(!"".equals(cookieValue))
            cookieValue = cookieValue.substring(0, cookieValue.length() - 1); // remove trailing char
        
        cookieManager.setCookie(QueueITDebugKey, cookieValue, null, null);
    }
    
    private static void logMoreRequestDetails(Map<String, String> debugEntries, HttpServletRequest request) {
        debugEntries.put("ServerUtcTime", Instant.now().toString());
        debugEntries.put("RequestIP", request.getRemoteAddr());
        debugEntries.put("RequestHttpHeader_Via", request.getHeader("via") != null ? request.getHeader("via") : "");
        debugEntries.put("RequestHttpHeader_Forwarded", request.getHeader("forwarded") != null ? request.getHeader("forwarded") : "");
        debugEntries.put("RequestHttpHeader_XForwardedFor", request.getHeader("x-forwarded-for") != null ? request.getHeader("x-forwarded-for") : "");
        debugEntries.put("RequestHttpHeader_XForwardedHost", request.getHeader("x-forwarded-host") != null ? request.getHeader("x-forwarded-host") : "");
        debugEntries.put("RequestHttpHeader_XForwardedProto", request.getHeader("x-forwarded-proto") != null ? request.getHeader("x-forwarded-proto") : "");        
    }
    
    private static boolean getIsDebug(String queueitToken, String secretKey) throws Exception
    {
        QueueUrlParams qParams = QueueParameterHelper.extractQueueParams(queueitToken);
        if (qParams != null && qParams.getRedirectType() != null && "debug".equals(qParams.getRedirectType().toLowerCase())) {
            String hash = HashHelper.generateSHA256Hash(secretKey, qParams.getQueueITTokenWithoutHash());
            return qParams.getHashCode().equals(hash);
        }
        return false;
    }
    
    private static String getOriginalUrl(HttpServletRequest request){
        return (request.getQueryString() != null) 
                ? String.join("", request.getRequestURL(), "?",request.getQueryString()) 
                : request.getRequestURL().toString();        
    }
    
    private static RequestValidationResult handleQueueAction(IntegrationConfigModel matchedConfig, String currentUrlWithoutQueueITToken, CustomerIntegration customerIntegrationInfo, String queueitToken, String customerId, HttpServletRequest request, HttpServletResponse response, String secretKey, Map<String, String> debugEntries) throws Exception {
        String targetUrl;
        switch (matchedConfig.RedirectLogic) {
            case "ForecedTargetUrl": // suuport for typo (fall through)
            case "ForcedTargetUrl":
                targetUrl = matchedConfig.ForcedTargetUrl;
                break;
            case "EventTargetUrl":
                targetUrl = "";
                break;
            default:
                targetUrl = currentUrlWithoutQueueITToken;
                break;
        }
        
        QueueEventConfig queueConfig = new QueueEventConfig();
        queueConfig.setQueueDomain(matchedConfig.QueueDomain);
        queueConfig.setCulture(matchedConfig.Culture);
        queueConfig.setEventId(matchedConfig.EventId);
        queueConfig.setExtendCookieValidity(matchedConfig.ExtendCookieValidity);
        queueConfig.setLayoutName(matchedConfig.LayoutName);
        queueConfig.setCookieValidityMinute(matchedConfig.CookieValidityMinute);
        queueConfig.setCookieDomain(matchedConfig.CookieDomain);
        queueConfig.setVersion(customerIntegrationInfo.Version);
        
        return resolveQueueRequestByLocalConfig(
                targetUrl, queueitToken, queueConfig, customerId, request, response, secretKey, debugEntries);
    }

    private static RequestValidationResult handleCancelAction(IntegrationConfigModel matchedConfig, CustomerIntegration customerIntegrationInfo, String currentUrlWithoutQueueITToken, String queueitToken, String customerId, HttpServletRequest request, HttpServletResponse response, String secretKey, Map<String, String> debugEntries) throws Exception {
        CancelEventConfig cancelConfig = new CancelEventConfig();
        cancelConfig.setQueueDomain(matchedConfig.QueueDomain);
        cancelConfig.setEventId(matchedConfig.EventId);
        cancelConfig.setCookieDomain(matchedConfig.CookieDomain);
        cancelConfig.setVersion(customerIntegrationInfo.Version);
        
        return cancelRequestByLocalConfig(
                currentUrlWithoutQueueITToken, queueitToken, cancelConfig, customerId, request, response, secretKey, debugEntries);
    }

    private static RequestValidationResult handleIgnoreAction(HttpServletRequest request, HttpServletResponse response) {
        IUserInQueueService userInQueueService = getUserInQueueService(request, response);
        return userInQueueService.getIgnoreActionResult();
    }
}

interface ICookieManager {

    void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain);
    String getCookie(String cookieName);
}

class CookieManager implements ICookieManager {

    HttpServletRequest request;
    HttpServletResponse response;

    public CookieManager(HttpServletRequest request,
            HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public void setCookie(String cookieName, String cookieValue, Integer expiration, String cookieDomain) {
        if(response == null)
            return;
        
        Cookie cookie = new Cookie(cookieName, cookieValue);
        if (cookieValue == null) {
            cookieValue = "";
        }
        try {
            cookie.setValue(URLEncoder.encode(cookieValue, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {}
        if(expiration != null)
            cookie.setMaxAge(expiration);
        cookie.setPath("/");
        if (!Utils.isNullOrWhiteSpace(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }

    @Override
    public String getCookie(String cookieName) {
        if(request == null)
            return null;
        
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                try {
                    return URLDecoder.decode(cookie.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException ex) { }
            }
        }
        return null;
    }
}
