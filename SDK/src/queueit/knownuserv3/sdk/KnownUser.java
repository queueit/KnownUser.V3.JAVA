package queueit.knownuserv3.sdk;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import queueit.knownuserv3.sdk.integrationconfig.*;


public class KnownUser {

    public static final String QueueITTokenKey = "queueittoken";
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
        if (matchedConfig == null) {
            return new RequestValidationResult(null, null, null);
        }

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

        EventConfig eventConfig = new EventConfig();
        eventConfig.setQueueDomain(matchedConfig.QueueDomain);
        eventConfig.setCulture(matchedConfig.Culture);
        eventConfig.setEventId(matchedConfig.EventId);
        eventConfig.setExtendCookieValidity(matchedConfig.ExtendCookieValidity);
        eventConfig.setLayoutName(matchedConfig.LayoutName);
        eventConfig.setCookieValidityMinute(matchedConfig.CookieValidityMinute);
        eventConfig.setCookieDomain(matchedConfig.CookieDomain);
        eventConfig.setVersion(customerIntegrationInfo.Version);

        return validateRequestByLocalEventConfig(targetUrl, queueitToken, eventConfig, customerId, request, response, secretKey);
    }

    public static RequestValidationResult validateRequestByLocalEventConfig(String targetUrl, String queueitToken, EventConfig eventConfig,
            String customerId, HttpServletRequest request,
            HttpServletResponse response, String secretKey) throws Exception {
        
        if (Utils.isNullOrWhiteSpace(customerId)) {
            throw new Exception("customerId can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new Exception("secretKey can not be null or empty.");
        }
        if (eventConfig == null) {
            throw new Exception("eventConfig can not be null.");
        }
        if (Utils.isNullOrWhiteSpace(eventConfig.getEventId())) {
            throw new Exception("EventId from eventConfig can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(eventConfig.getQueueDomain())) {
            throw new Exception("QueueDomain from eventConfig can not be null or empty.");
        }
        if (eventConfig.getCookieValidityMinute() <= 0) {
            throw new Exception("cookieValidityMinute from eventConfig should be greater than 0.");
        }
        if (queueitToken == null) {
            queueitToken = "";
        }

        IUserInQueueService userInQueueService = createUserInQueueService(request, response);
        return userInQueueService.validateRequest(targetUrl, queueitToken, eventConfig, customerId, secretKey);
    }

    public static void cancelQueueCookie(String eventId,
            String cookieDomain,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        if (Utils.isNullOrWhiteSpace(eventId)) {
            throw new Exception("eventId can not be null or empty.");
        }
        
        IUserInQueueService userInQueueService = createUserInQueueService(request, response);
        userInQueueService.cancelQueueCookie(eventId, cookieDomain);
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
    public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        if (cookieValue == null) {
            cookieValue = "";
        }
        cookie.setValue(cookieValue);
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
