package queueit.knownuserv3.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import queueit.knownuserv3.sdk.integrationconfig.CustomerIntegration;
import queueit.knownuserv3.sdk.integrationconfig.IntegrationConfigModel;
import queueit.knownuserv3.sdk.integrationconfig.TriggerModel;
import queueit.knownuserv3.sdk.integrationconfig.TriggerPart;

public class KnownUserTest {

    class UserInQueueServiceMock implements IUserInQueueService {

        public ArrayList<ArrayList<String>> validateQueueRequestCalls = new ArrayList<>();
        public ArrayList<ArrayList<String>> validateCancelRequestCalls = new ArrayList<>();
        public ArrayList<ArrayList<String>> extendQueueCookieCalls = new ArrayList<>();
        public ArrayList<ArrayList<String>> getIgnoreActionResultCalls = new ArrayList<>();

        @Override
        public RequestValidationResult validateQueueRequest(String targetUrl, String queueitToken, QueueEventConfig config, String customerId, String secretKey) throws Exception {
            ArrayList<String> args = new ArrayList<>();
            args.add(targetUrl);
            args.add(queueitToken);
            args.add(config.getCookieDomain() + ":"
                    + config.getLayoutName() + ":"
                    + config.getCulture() + ":"
                    + config.getEventId() + ":"
                    + config.getQueueDomain() + ":"
                    + config.getExtendCookieValidity() + ":"
                    + config.getCookieValidityMinute() + ":"
                    + config.getVersion());
            args.add(customerId);
            args.add(secretKey);
            validateQueueRequestCalls.add(args);

            return new RequestValidationResult("Queue", "", "", "", "");
        }

        @Override
        public RequestValidationResult validateCancelRequest(
                String targetUrl,
                CancelEventConfig config,
                String customerId,
                String secretKey) throws Exception {

            ArrayList<String> args = new ArrayList<>();
            args.add(targetUrl);
            args.add(config.getCookieDomain() + ":"
                    + config.getEventId() + ":"
                    + config.getQueueDomain() + ":"
                    + config.getVersion());
            args.add(customerId);
            args.add(secretKey);
            validateCancelRequestCalls.add(args);

            return new RequestValidationResult("Cancel", "", "", "", "");
        }

        @Override
        public void extendQueueCookie(String eventId, int cookieValidityMinute, String cookieDomain, String secretKey) {
            ArrayList<String> args = new ArrayList<>();
            args.add(eventId);
            args.add(Integer.toString(cookieValidityMinute));
            args.add(cookieDomain);
            args.add(secretKey);
            extendQueueCookieCalls.add(args);
        }

        @Override
        public RequestValidationResult getIgnoreActionResult() {
            getIgnoreActionResultCalls.add(new ArrayList<>());
            return new RequestValidationResult("Ignore", "", "", "", "");
        }
    }

    @Test
    public void cancelRequestByLocalConfigTest() throws Exception {
        // Arrange		
        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);

        // Act
        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("url", "queueitToken", cancelEventConfig, "customerid", requestMock, responseMock, "secretkey");

        // Assert
        assertTrue("url".equals(mock.validateCancelRequestCalls.get(0).get(0)));
        assertTrue("cookiedomain:eventid:queuedomain:1".equals(mock.validateCancelRequestCalls.get(0).get(1)));
        assertTrue("customerid".equals(mock.validateCancelRequestCalls.get(0).get(2)));
        assertTrue("secretkey".equals(mock.validateCancelRequestCalls.get(0).get(3)));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void CancelRequestByLocalConfig_AjaxCall_Test() throws Exception {
        // Arrange
        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);

        // Act
        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("url", "queueitToken", cancelEventConfig, "customerid", requestMock, null, "secretkey");

        // Assert
        assertTrue("http://url".equals(mock.validateCancelRequestCalls.get(0).get(0)));
        assertTrue("cookiedomain:eventid:queuedomain:1".equals(mock.validateCancelRequestCalls.get(0).get(1)));
        assertTrue("customerid".equals(mock.validateCancelRequestCalls.get(0).get(2)));
        assertTrue("secretkey".equals(mock.validateCancelRequestCalls.get(0).get(3)));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void cancelRequestByLocalConfigDebugCookieLoggingTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.RequestURL = "requestUrl";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        // Act
        String secretKey = "secretkey";
        String queueittoken = QueueITTokenGenerator.generateToken("eventId", secretKey);

        KnownUser.cancelRequestByLocalConfig("url", queueittoken, cancelEventConfig, "customerId", requestMock, responseMock, secretKey);

        // Assert
        assertTrue(responseMock.addedCookies.size() == 1);
        assertTrue(responseMock.addedCookies.get(0).getName().equals(KnownUser.QueueITDebugKey));
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(), "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=requestUrl"));
        assertTrue(decodedCookieValue.contains("|CancelConfig=EventId:eventid"));
        assertTrue(decodedCookieValue.contains("&Version:1"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:queuedomain"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:cookiedomain"));
        assertTrue(decodedCookieValue.contains("|QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("|TargetUrl=url"));
        assertTrue(decodedCookieValue.contains("|RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedProto=https"));
    }

    @Test
    public void cancelRequestByLocalConfigNullQueueDomainTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setCookieDomain("cookieDomain");
        cancelEventConfig.setVersion(12);

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", cancelEventConfig, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "QueueDomain from cancelConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigEventIdNullTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("domain");
        cancelEventConfig.setVersion(12);

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", cancelEventConfig, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "EventId from cancelConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigCancelEventConfigNullTest() {
        // Arrange        
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", null, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "cancelConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigCustomerIdNullTest() {
        // Arrange    
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", new CancelEventConfig(), null, new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "customerId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigSecretKeyNullTest() {
        // Arrange     
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", new CancelEventConfig(), "customerId", new HttpServletRequestMock(), null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void CancelRequestByLocalConfigTargetUrlNullTest() {
        // Arrange     
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig(null, "queueitToken", new CancelEventConfig(), "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "targetUrl can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieNullEventIdTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.extendQueueCookie(null, 0, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "eventId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.extendQueueCookieCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieInvalidCookieValidityMinutesTest() {
        // Arrange        
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.extendQueueCookie("eventId", 0, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "cookieValidityMinute should be greater than 0.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.extendQueueCookieCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieNullSecretKeyTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.extendQueueCookie("eventId", 20, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.extendQueueCookieCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        // Act
        KnownUser.extendQueueCookie("eventId", 20, "cookieDomain", null, null, "secretKey");

        // Assert
        assertTrue("eventId".equals(mock.extendQueueCookieCalls.get(0).get(0)));
        assertTrue("20".equals(mock.extendQueueCookieCalls.get(0).get(1)));
        assertTrue("cookieDomain".equals(mock.extendQueueCookieCalls.get(0).get(2)));
        assertTrue("secretKey".equals(mock.extendQueueCookieCalls.get(0).get(3)));
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullCustomerIdTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", null, null, new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "customerId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullSecretKeyTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", null, "customerId", new HttpServletRequestMock(), null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullEventConfigTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", null, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "eventConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullEventIdTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        //eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "EventId from queueConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullQueueDomainTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        //eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "QueueDomain from queueConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigInvalidCookieValidityMinuteTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        //eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId", new HttpServletRequestMock(), null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "cookieValidityMinute from queueConfig should be greater than 0.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue("targetUrl".equals(mock.validateQueueRequestCalls.get(0).get(0)));
        assertTrue("queueitToken".equals(mock.validateQueueRequestCalls.get(0).get(1)));
        assertTrue("cookieDomain:layoutName:culture:eventId:queueDomain:true:10:12".equals(mock.validateQueueRequestCalls.get(0).get(2)));
        assertTrue("customerId".equals(mock.validateQueueRequestCalls.get(0).get(3)));
        assertTrue("secretKey".equals(mock.validateQueueRequestCalls.get(0).get(4)));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void resolveQueueRequestByLocalConfigAjaxCallTest() throws Exception {
        // Arrange
        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId", requestMock, null, "secretKey");

        // Assert
        assertTrue("http://url".equals(mock.validateQueueRequestCalls.get(0).get(0)));
        assertTrue("queueitToken".equals(mock.validateQueueRequestCalls.get(0).get(1)));
        assertTrue("cookieDomain:layoutName:culture:eventId:queueDomain:true:10:12".equals(mock.validateQueueRequestCalls.get(0).get(2)));
        assertTrue("customerId".equals(mock.validateQueueRequestCalls.get(0).get(3)));
        assertTrue("secretKey".equals(mock.validateQueueRequestCalls.get(0).get(4)));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void resolveQueueRequestByLocalConfigDebugCookieLoggingTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        QueueEventConfig queueConfig = new QueueEventConfig();
        queueConfig.setCookieDomain("cookieDomain");
        queueConfig.setLayoutName("layoutName");
        queueConfig.setCulture("culture");
        queueConfig.setEventId("eventId");
        queueConfig.setQueueDomain("queueDomain");
        queueConfig.setExtendCookieValidity(true);
        queueConfig.setCookieValidityMinute(10);
        queueConfig.setVersion(12);

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.RequestURL = "requestUrl";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        // Act
        String secretKey = "secretkey";
        String queueittoken = QueueITTokenGenerator.generateToken("eventId", secretKey);

        KnownUser.resolveQueueRequestByLocalConfig("targetUrl", queueittoken, queueConfig, "customerId", requestMock, responseMock, secretKey);

        // Assert
        assertTrue(responseMock.addedCookies.size() == 1);
        assertTrue(responseMock.addedCookies.get(0).getName().equals(KnownUser.QueueITDebugKey));
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(), "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=requestUrl"));
        assertTrue(decodedCookieValue.contains("|QueueConfig=EventId:eventId"));
        assertTrue(decodedCookieValue.contains("&Version:12"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:queueDomain"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:cookieDomain"));
        assertTrue(decodedCookieValue.contains("&ExtendCookieValidity:true"));
        assertTrue(decodedCookieValue.contains("&CookieValidityMinute:10"));
        assertTrue(decodedCookieValue.contains("&LayoutName:layoutName"));
        assertTrue(decodedCookieValue.contains("&Culture:culture"));
        assertTrue(decodedCookieValue.contains("|QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("|TargetUrl=targetUrl"));
        assertTrue(decodedCookieValue.contains("|RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedProto=https"));
    }

    @Test
    public void validateRequestByIntegrationConfigEmptyCurrentUrlTest() {
        // Arrange        
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByIntegrationConfig("", null, null, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "currentUrlWithoutQueueITToken can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByIntegrationConfigEmptyIntegrationsConfigTest() {
        // Arrange        
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByIntegrationConfig("currentUrl", "queueitToken", null, null, new HttpServletRequestMock(), null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "customerIntegrationInfo can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByIntegrationConfigQueueActionTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart1, triggerPart2};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;
        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.UserAgent = "googlebot";

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", httpContextMock, null, "secretKey");

        // Assert
        assertTrue(mock.validateQueueRequestCalls.size() == 1);
        assertTrue("http://test.com?event1=true".equals(mock.validateQueueRequestCalls.get(0).get(0)));
        assertTrue("queueitToken".equals(mock.validateQueueRequestCalls.get(0).get(1)));
        assertTrue(".test.com:Christmas Layout by Queue-it:da-DK:event1:knownusertest.queue-it.net:true:20:3".equals(mock.validateQueueRequestCalls.get(0).get(2)));
        assertTrue("customerId".equals(mock.validateQueueRequestCalls.get(0).get(3)));
        assertTrue("secretKey".equals(mock.validateQueueRequestCalls.get(0).get(4)));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigQueueActionAjaxCallTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart1, triggerPart2};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", requestMock, null, "secretKey");

        // Assert
        assertTrue(mock.validateQueueRequestCalls.size() == 1);
        assertTrue("http://url".equals(mock.validateQueueRequestCalls.get(0).get(0)));
        assertTrue("queueitToken".equals(mock.validateQueueRequestCalls.get(0).get(1)));
        assertTrue(".test.com:Christmas Layout by Queue-it:da-DK:event1:knownusertest.queue-it.net:true:20:3".equals(mock.validateQueueRequestCalls.get(0).get(2)));
        assertTrue("customerId".equals(mock.validateQueueRequestCalls.get(0).get(3)));
        assertTrue("secretKey".equals(mock.validateQueueRequestCalls.get(0).get(4)));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigDebugCookieLoggingTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart1, triggerPart2};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";
        requestMock.RequestURL = "requestUrl";

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        // Act
        String secretKey = "secretkey";
        String queueittoken = QueueITTokenGenerator.generateToken("eventId", secretKey);

        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", queueittoken, customerIntegration, "customerId", requestMock, responseMock, secretKey);

        // Assert
        assertTrue(responseMock.addedCookies.size() == 1);
        assertTrue(responseMock.addedCookies.get(0).getName().equals(KnownUser.QueueITDebugKey));
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(), "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=requestUrl"));
        assertTrue(decodedCookieValue.contains("|PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("|ConfigVersion=3"));
        assertTrue(decodedCookieValue.contains("|QueueConfig=EventId:event1"));
        assertTrue(decodedCookieValue.contains("&Version:3"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:knownusertest.queue-it.net"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:.test.com"));
        assertTrue(decodedCookieValue.contains("&ExtendCookieValidity:true"));
        assertTrue(decodedCookieValue.contains("&CookieValidityMinute:20"));
        assertTrue(decodedCookieValue.contains("&LayoutName:Christmas Layout by Queue-it"));
        assertTrue(decodedCookieValue.contains("&Culture:da-DK"));
        assertTrue(decodedCookieValue.contains("|QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("|TargetUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("|MatchedConfig=event1action"));
    }

    @Test
    public void validateRequestByIntegrationConfigNotMatchTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[0];
        customerIntegration.Version = 3;

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue(mock.validateQueueRequestCalls.isEmpty());
        assertTrue(!result.doRedirect());
    }

    @Test
    public void validateRequestByIntegrationConfigNotMatchDebugCookieLoggingTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[0];
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.RequestURL = "requestUrl";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        // Act
        String secretKey = "secretkey";
        String queueittoken = QueueITTokenGenerator.generateToken("eventId", secretKey);

        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", queueittoken, customerIntegration, "customerId", requestMock, responseMock, secretKey);

        // Assert
        assertTrue(responseMock.addedCookies.size() == 1);
        assertTrue(responseMock.addedCookies.get(0).getName().equals(KnownUser.QueueITDebugKey));
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(), "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=requestUrl"));
        assertTrue(decodedCookieValue.contains("|PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("ConfigVersion=3"));
        assertTrue(decodedCookieValue.contains("|QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("|RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("|RequestHttpHeader_XForwardedProto=https"));
    }

    @Test
    public void validateRequestByIntegrationConfigForcedTargeturlTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "ForcedTargetUrl";
        config.ForcedTargetUrl = "http://forcedtargeturl.com";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue(mock.validateQueueRequestCalls.size() == 1);
        assertTrue("http://forcedtargeturl.com".equals(mock.validateQueueRequestCalls.get(0).get(0)));
    }

    @Test
    public void validateRequestByIntegrationConfigForecedTargeturlTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "ForecedTargetUrl";
        config.ForcedTargetUrl = "http://forcedtargeturl.com";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue(mock.validateQueueRequestCalls.size() == 1);
        assertTrue("http://forcedtargeturl.com".equals(mock.validateQueueRequestCalls.get(0).get(0)));
    }

    @Test
    public void validateRequestByIntegrationConfigEventTargetUrl() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "EventTargetUrl";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue(mock.validateQueueRequestCalls.size() == 1);
        assertTrue("".equals(mock.validateQueueRequestCalls.get(0).get(0)));
    }

    @Test
    public void validateRequestByIntegrationConfigIgnoreAction() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.IGNORE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue(mock.getIgnoreActionResultCalls.size() == 1);
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigAjaxCallIgnoreAction() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.IGNORE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "url");

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", requestMock, null, "secretKey");

        // Assert
        assertTrue(mock.getIgnoreActionResultCalls.size() == 1);
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigCancelAction() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.CANCEL_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", new HttpServletRequestMock(), null, "secretKey");

        // Assert
        assertTrue("http://test.com?event1=true".equals(mock.validateCancelRequestCalls.get(0).get(0)));
        assertTrue("cookiedomain:event1:queuedomain:3".equals(mock.validateCancelRequestCalls.get(0).get(1)));
        assertTrue("customerId".equals(mock.validateCancelRequestCalls.get(0).get(2)));
        assertTrue("secretKey".equals(mock.validateCancelRequestCalls.get(0).get(3)));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigAjaxCallCancelAction() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[]{triggerPart};

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.CANCEL_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");

        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", requestMock, null, "secretKey");

        // Assert
        assertTrue("http://url".equals(mock.validateCancelRequestCalls.get(0).get(0)));
        assertTrue("cookiedomain:event1:queuedomain:3".equals(mock.validateCancelRequestCalls.get(0).get(1)));
        assertTrue("customerId".equals(mock.validateCancelRequestCalls.get(0).get(2)));
        assertTrue("secretKey".equals(mock.validateCancelRequestCalls.get(0).get(3)));
        assertTrue(result.isAjaxResult);
    }

    class HttpServletRequestMock implements HttpServletRequest {

        public Cookie[] CookiesValue;
        public String UserAgent;
        public String RequestURL;
        public String QueryString;
        public String RemoteAddr;
        public HashMap Headers;

        public HttpServletRequestMock() {
            this.Headers = new HashMap();
        }

        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Cookie[] getCookies() {
            return this.CookiesValue;
        }

        @Override
        public long getDateHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getHeader(String key) {
            if ("User-Agent".equals(key)) {
                return this.UserAgent;
            }

            String value = (String) this.Headers.get(key);

            if (value == null) {
                value = "";
            }

            return value;
        }

        @Override
        public Enumeration<String> getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getIntHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getPathInfo() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getQueryString() {
            return this.QueryString;
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isUserInRole(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRequestURI() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer(this.RequestURL);
        }

        @Override
        public String getServletPath() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public HttpSession getSession(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String changeSessionId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean authenticate(HttpServletResponse hsr) throws IOException, ServletException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void login(String string, String string1) throws ServletException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void logout() throws ServletException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Part getPart(String string) throws IOException, ServletException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> type) throws IOException, ServletException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getAttribute(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long getContentLengthLong() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getParameter(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String[] getParameterValues(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRemoteAddr() {
            return RemoteAddr;
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setAttribute(String string, Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeAttribute(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getRealPath(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    class HttpServletResponseMock implements HttpServletResponse {

        ArrayList<Cookie> addedCookies = new ArrayList<>();

        @Override
        public void addCookie(Cookie cookie) {
            addedCookies.add(cookie);
        }

        @Override
        public boolean containsHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String encodeURL(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String encodeRedirectURL(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String encodeUrl(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String encodeRedirectUrl(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void sendError(int i, String string) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void sendError(int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void sendRedirect(String string) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setDateHeader(String string, long l) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addDateHeader(String string, long l) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setHeader(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addHeader(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setIntHeader(String string, int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addIntHeader(String string, int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setStatus(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setStatus(int i, String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getStatus() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection<String> getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection<String> getHeaderNames() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setCharacterEncoding(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setContentLength(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setContentLengthLong(long l) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setContentType(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setBufferSize(int i) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getBufferSize() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void flushBuffer() throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void resetBuffer() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setLocale(Locale locale) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static class QueueITTokenGenerator {

        public static String generateToken(
                String eventId,
                String secretKey) throws Exception {

            ArrayList<String> paramList = new ArrayList<>();
            paramList.add(QueueParameterHelper.EventIdKey + QueueParameterHelper.KeyValueSeparatorChar + eventId);
            paramList.add(QueueParameterHelper.RedirectTypeKey + QueueParameterHelper.KeyValueSeparatorChar + "debug");

            String tokenWithoutHash = String.join(QueueParameterHelper.KeyValueSeparatorGroupChar, paramList);
            String hash = HashHelper.generateSHA256Hash(secretKey, tokenWithoutHash);
            String token = tokenWithoutHash + QueueParameterHelper.KeyValueSeparatorGroupChar + QueueParameterHelper.HashKey + QueueParameterHelper.KeyValueSeparatorChar + hash;
            return token;
        }
    }
}
