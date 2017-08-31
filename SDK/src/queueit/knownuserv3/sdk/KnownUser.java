package queueit.knownuserv3.sdk;
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

    private static IUserInQueueService createUserInQueueService(HttpServletRequest request, HttpServletResponse response) {
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
                debugEntries.put("configVersion", Integer.toString(customerIntegrationInfo.Version));
                debugEntries.put("pureUrl", currentUrlWithoutQueueITToken);
                debugEntries.put("queueitToken", queueitToken);
                debugEntries.put("OriginalURL", request.getRequestURL().toString());                
            }

            if (Utils.isNullOrWhiteSpace(currentUrlWithoutQueueITToken)) {
                throw new Exception("currentUrlWithoutQueueITToken can not be null or empty.");
            }
            if (customerIntegrationInfo == null) {
                throw new KnowUserException("customerIntegrationInfo can not be null.");
            }

            Cookie[] cookies = request != null ? request.getCookies() : new Cookie[0];        

            IntegrationEvaluator configEvaluater = new IntegrationEvaluator();
            String userAgent = request.getHeader("User-Agent");

            IntegrationConfigModel matchedConfig = configEvaluater.getMatchedIntegrationConfig(
                    customerIntegrationInfo, currentUrlWithoutQueueITToken, cookies, userAgent != null ? userAgent : "");

            if (isDebug) {
                String matchedConfigName = (matchedConfig != null) ? matchedConfig.Name : "NULL";
                debugEntries.put("matchedConfig", matchedConfigName);                
            }

            if (matchedConfig == null) {
                return new RequestValidationResult(null, null, null, null);
            }

            if(Utils.isNullOrWhiteSpace(matchedConfig.ActionType) || ActionType.QUEUE_ACTION.equals(matchedConfig.ActionType)) {
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
            // CancelQueueAction
            else {
                CancelEventConfig cancelConfig = new CancelEventConfig();
                cancelConfig.setQueueDomain(matchedConfig.QueueDomain);
                cancelConfig.setEventId(matchedConfig.EventId);
                cancelConfig.setCookieDomain(matchedConfig.CookieDomain);
                cancelConfig.setVersion(customerIntegrationInfo.Version);

                return cancelRequestByLocalConfig(
                        currentUrlWithoutQueueITToken, queueitToken, cancelConfig, customerId, request, response, secretKey, debugEntries);
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
            debugEntries.put("targetUrl", targetUrl);
            debugEntries.put("queueitToken", queueitToken);
            debugEntries.put("cancelConfig", cancelConfig != null ? cancelConfig.toString() : "NULL");
            debugEntries.put("OriginalURL", request.getRequestURL().toString());
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
        
        IUserInQueueService userInQueueService = createUserInQueueService(request, response);
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
            debugEntries.put("targetUrl", targetUrl);
            debugEntries.put("queueitToken", queueitToken);
            debugEntries.put("queueConfig", queueConfig != null ? queueConfig.toString() : "NULL");
            debugEntries.put("OriginalURL", request.getRequestURL().toString());            
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

        IUserInQueueService userInQueueService = createUserInQueueService(request, response);
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
        
        IUserInQueueService userInQueueService = createUserInQueueService(request, response);
        userInQueueService.extendQueueCookie(eventId, cookieValidityMinute, cookieDomain, secretKey);
    }
    
    private static void setDebugCookie(Map<String, String> debugEntries, HttpServletRequest request, HttpServletResponse response) {
        if(debugEntries.isEmpty())
            return;
        
        ICookieManager cookieManager = new CookieManager(request, response);
        String cookieValue = "";        
        for (Map.Entry<String, String> entry : debugEntries.entrySet()) {
            cookieValue += (entry.getKey() + "=" + entry.getValue() + "&");            
        }
        if(!"".equals(cookieValue))
            cookieValue = cookieValue.substring(0, cookieValue.length() - 1); // remove trailing &
        cookieManager.setCookie(QueueITDebugKey, cookieValue, null, null);
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
        cookie.setValue(cookieValue);
        if(expiration != null)
            cookie.setMaxAge(expiration);
        cookie.setPath("/");
        if (!Utils.isNullOrWhiteSpace(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }

        cookie.setHttpOnly(true);
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
                return cookie.getValue();
            }
        }
        return null;
    }
}
