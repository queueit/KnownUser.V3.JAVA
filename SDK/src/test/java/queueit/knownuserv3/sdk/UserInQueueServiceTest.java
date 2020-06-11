
package queueit.knownuserv3.sdk;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UserInQueueServiceTest {

    // ExtendableCookie Cookie
    @Test
    public void validateQueueRequest_ValidState_ExtendableCookie_NoCookieExtensionFromConfig_DoNotRedirectDoNotStoreCookieWithExtension()
            throws Exception {

        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setActionName("QueueAction");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes,
                    String cookieDomainString, String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(true, true, "queueId", null, "queue");
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateQueueRequest("url", "token", config, "testCustomer", "key");
        assertTrue(!result.doRedirect());
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(result.getEventId().equals("e1"));
        assertTrue(result.getQueueId().equals("queueId"));
        assertTrue(result.getRedirectType().equals("queue"));
        assertEquals(result.getActionName(), config.getActionName());
    }

    @Test
    public void validateQueueRequest_ValidState_ExtendableCookie_CookieExtensionFromConfig_DoNotRedirect_DoStoreCookieWithExtension()
            throws Exception {

        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(true);
        config.setCookieDomain(".testdomain.com");
        config.setActionName("QueueAction");
        final HashMap<String, HashMap<String, Object>> callInfo = new HashMap<String, HashMap<String, Object>>();
        callInfo.put("firstCall", new HashMap<String, Object>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("eventId", eventId);
                info.put("fixedCookieValidityMinutes", fixedCookieValidityMinutes);
                info.put("redirectType", redirectType);
                info.put("cookieDomain", cookieDomain);
                info.put("queueId", queueId);
                info.put("customerSecretKey", customerSecretKey);
                callInfo.put("firstCall", info);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(true, true, "queueId", null, "queue");

            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateQueueRequest("url", "token", config, "testCustomer", "key");
        assertTrue(!result.doRedirect());
        assertTrue(callInfo.get("firstCall").get("queueId").equals("queueId"));
        assertTrue(callInfo.get("firstCall").get("eventId").equals("e1"));
        assertTrue(callInfo.get("firstCall").get("redirectType").equals("queue"));
        assertTrue(callInfo.get("firstCall").get("customerSecretKey").equals("key"));
        assertTrue(callInfo.get("firstCall").get("cookieDomain").equals(".testdomain.com"));
        assertEquals(result.getActionName(), config.getActionName());
    }

    @Test
    public void validateQueueRequest_ValidState_NoExtendableCookie_DoNotRedirect_DoNotStoreCookieWithExtension()
            throws Exception {

        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(true);
        config.setActionName("QueueAction");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(true, true, "queueId", "3", "idle");
            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateQueueRequest("url", "token", config, "testCustomer", "key");
        assertTrue(!result.doRedirect());
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(result.getEventId().equals("e1"));
        assertTrue(result.getQueueId().equals("queueId"));
        assertEquals(result.getActionName(), config.getActionName());
    }

    @Test
    public void ValidateQueueRequest_NoCookie_TampredToken_RedirectToErrorPageWithHashError_DoNotStoreCookie()
            throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setVersion(100);
        config.setActionName("QueueAction");
        config.setCookieDomain("TestDomain");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookieWasCalled", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String customerKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String queueitToken = QueueITTokenGenerator.generateToken(new Date(), "e1", false, 20, customerKey, "queue");
        queueitToken = queueitToken.replace("false", "true");

        String targetUrl = "http://test.test.com?b=h";
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/error/hash/?c=testCustomer&e=e1" + "&ver=" + knownUserVersion
                + "&cver=100" + "&man=" + config.getActionName() + "&queueittoken=" + queueitToken + "&t="
                + URLEncoder.encode(targetUrl, "UTF-8");

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertTrue(result.doRedirect());

        Pattern pattern = Pattern.compile("&ts=[^&]*");
        Matcher matcher = pattern.matcher(result.getRedirectUrl());
        assertTrue(matcher.find());
        String serverTimestamp = matcher.group().replace("&ts=", "");
        long timestampDiffInSecs = (System.currentTimeMillis() / 1000) - Long.parseLong(serverTimestamp);
        assertTrue(timestampDiffInSecs < 10);

        String redirectUrl = matcher.replaceAll("");
        assertTrue(redirectUrl.toUpperCase().equals(expectedErrorUrl.toUpperCase()));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookieWasCalled") == null);
    }

    @Test
    public void ValidateQueueRequest_NoCookie_ExpiredTimeStampInToken_RedirectToErrorPageWithTimeStampError_DoNotStoreCookie()
            throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setVersion(100);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");
        String customerKey = "4e1db821-a825-49da-acd0-5d376f2068db";

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        Date date = new Date();
        date.setTime(date.getTime() - 1000 * 60 * 60);
        String queueitToken = QueueITTokenGenerator.generateToken(date, "e1", true, 20, customerKey, "queue");

        String targetUrl = "http://test.test.com?b=h";
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/error/timestamp/?c=testCustomer&e=e1" + "&ver="
                + knownUserVersion + "&cver=100" + "&man=" + config.getActionName() + "&queueittoken=" + queueitToken
                + "&t=" + URLEncoder.encode(targetUrl, "UTF-8");

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertTrue(result.doRedirect());

        Pattern pattern = Pattern.compile("&ts=[^&]*");
        Matcher matcher = pattern.matcher(result.getRedirectUrl());
        assertTrue(matcher.find());
        String serverTimestamp = matcher.group().replace("&ts=", "");
        long timestampDiffInSecs = (System.currentTimeMillis() / 1000) - Long.parseLong(serverTimestamp);
        assertTrue(timestampDiffInSecs < 10);

        String redirectUrl = matcher.replaceAll("");
        assertTrue(redirectUrl.toUpperCase().equals(expectedErrorUrl.toUpperCase()));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie") == null);
    }

    @Test
    public void ValidateQueueRequest_NoCookie_EventIdMismatch_RedirectToErrorPageWithEventIdMissMatchError_DoNotStoreCookie()
            throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e2");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setVersion(10);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");
        String customerKey = "4e1db821-a825-49da-acd0-5d376f2068db";

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 60 * 60);
        String queueitToken = QueueITTokenGenerator.generateToken(date, "e1", true, null, customerKey, "queue");

        String targetUrl = "http://test.test.com?b=h";
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/error/eventid/?c=testCustomer&e=e2" + "&ver="
                + knownUserVersion + "&cver=10" + "&man=" + config.getActionName() + "&queueittoken=" + queueitToken
                + "&t=" + URLEncoder.encode(targetUrl, "UTF-8");

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertTrue(result.doRedirect());

        Pattern pattern = Pattern.compile("&ts=[^&]*");
        Matcher matcher = pattern.matcher(result.getRedirectUrl());
        assertTrue(matcher.find());
        String serverTimestamp = matcher.group().replace("&ts=", "");
        long timestampDiffInSecs = (System.currentTimeMillis() / 1000) - Long.parseLong(serverTimestamp);
        assertTrue(timestampDiffInSecs < 10);

        String redirectUrl = matcher.replaceAll("");
        assertTrue(redirectUrl.toUpperCase().equals(expectedErrorUrl.toUpperCase()));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie") == null);
    }

    @Test
    public void ValidateQueueRequest_NoCookie_ValidToken_ExtendableCookie_DoNotRedirect_StoreExtendableCookie()
            throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setCookieDomain(".testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setActionName("QueueAction");
        String customerKey = "4e1db821-a825-49da-acd0-5d376f2068db";

        final HashMap<String, HashMap<String, Object>> callInfo = new HashMap<String, HashMap<String, Object>>();
        callInfo.put("firstCall", new HashMap<String, Object>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("eventId", eventId);
                info.put("fixedCookieValidityMinutes", fixedCookieValidityMinutes);
                info.put("cookieDomain", cookieDomain);
                info.put("redirectType", redirectType);
                info.put("customerSecretKey", customerSecretKey);
                callInfo.put("firstCall", info);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                HashMap<String, Object> obj = new HashMap<String, Object>();
                obj.put("eventId", eventId);
                obj.put("cookieDomain", cookieDomain);
                callInfo.put("cancelQueueCookieWasCalled", obj);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 60 * 60);
        String queueitToken = QueueITTokenGenerator.generateToken(date, "e1", true, null, customerKey, "queue");

        String targetUrl = "http://test.test.com?b=h";

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertTrue(!result.doRedirect());
        assertTrue(callInfo.get("firstCall").get("eventId").equals(config.getEventId()));
        assertTrue(callInfo.get("firstCall").get("fixedCookieValidityMinutes") == null);
        assertTrue(callInfo.get("firstCall").get("cookieDomain").equals(config.getCookieDomain()));
        assertTrue(callInfo.get("firstCall").get("redirectType").equals("queue"));
        assertTrue(callInfo.get("firstCall").get("customerSecretKey").equals(customerKey));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(callInfo.get("cancelQueueCookieWasCalled") == null);
    }

    @Test
    public void ValidateQueueRequest_NoCookie_ValidToken_CookieValidityMinuteFromToken_DoNotRedirect_StoreNonExtendableCookie()
            throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("eventid");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(true);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");
        String customerKey = "secretekeyofuser";

        final HashMap<String, HashMap<String, Object>> callInfo = new HashMap<String, HashMap<String, Object>>();
        callInfo.put("firstCall", new HashMap<String, Object>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("eventId", eventId);
                info.put("fixedCookieValidityMinutes", fixedCookieValidityMinutes);
                info.put("cookieDomain", cookieDomain);
                info.put("redirectType", redirectType);
                info.put("customerSecretKey", customerSecretKey);
                callInfo.put("firstCall", info);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                HashMap<String, Object> obj = new HashMap<String, Object>();
                obj.put("eventId", eventId);
                obj.put("cookieDomain", cookieDomain);
                callInfo.put("cancelQueueCookieWasCalled", obj);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String queueitToken = "e_eventid~q_f8757c2d-34c2-4639-bef2-1736cdd30bbb~ri_34678c2d-34c2-4639-bef2-1736cdd30bbb~ts_1797033600~ce_False~cv_3~rt_DirectLink~h_5ee2babc3ac9fae9d80d5e64675710c371876386e77209f771007dc3e093e326";
        String targetUrl = "http://test.test.com?b=h";

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertTrue(!result.doRedirect());
        assertTrue(callInfo.get("firstCall").get("eventId").equals(config.getEventId()));
        assertTrue(callInfo.get("firstCall").get("redirectType").equals("DirectLink"));
        assertTrue(callInfo.get("firstCall").get("cookieDomain").equals(config.getCookieDomain()));
        assertTrue(callInfo.get("firstCall").get("fixedCookieValidityMinutes").equals(3));
        assertTrue(callInfo.get("firstCall").get("customerSecretKey").equals(customerKey));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(callInfo.get("cancelQueueCookieWasCalled") == null);
    }

    @Test
    public void ValidateQueueRequest_NoCookie_WithoutToken_RedirectToQueue() throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setLayoutName("testlayout");
        config.setVersion(10);
        config.setActionName("QueueAction");
        config.setQueueDomain("testDomain.com");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String targetUrl = "http://test.test.com?b=h";
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/?c=testCustomer&e=e1" + "&ver=" + knownUserVersion
                + "&cver=10" + "&man=" + config.getActionName() + "&l=" + config.getLayoutName() + "&t="
                + URLEncoder.encode(targetUrl, "UTF-8");

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, "", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertTrue(result.getRedirectUrl().toUpperCase().equals(expectedErrorUrl.toUpperCase()));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie") == null);
    }

    @Test
    public void ValidateQueueRequest_NoCookie_WithoutToken_RedirectToQueue_NoTargetUrl() throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setLayoutName("testlayout");
        config.setVersion(10);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/?c=testCustomer&e=e1" + "&ver=" + knownUserVersion
                + "&cver=10" + "&man=" + config.getActionName() + "&l=" + config.getLayoutName();

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(null, "", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertTrue(result.getRedirectUrl().toUpperCase().equals(expectedErrorUrl.toUpperCase()));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie") == null);
    }

    @Test
    public void ValidateQueueRequest_InvalidCookie_WithoutToken_RedirectToQueue_NoTargetUrl() throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setLayoutName("testlayout");
        config.setVersion(10);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(true, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/?c=testCustomer&e=e1" + "&ver=" + knownUserVersion
                + "&cver=10" + "&man=" + config.getActionName() + "&l=" + config.getLayoutName();

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(null, "", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertTrue(result.getRedirectUrl().toUpperCase().equals(expectedErrorUrl.toUpperCase()));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie"));
    }

    @Test
    public void ValidateQueueRequest_NoCookie_InValidToken() throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setLayoutName("testlayout");
        config.setVersion(10);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(false, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String targetUrl = "http://test.test.com/?b=h";
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl,
                "ts_sasa~cv_adsasa~ce_falwwwse~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertTrue(result.getRedirectUrl().startsWith("https://testDomain.com/error/hash/?c=testCustomer&e=e1&ver="
                + knownUserVersion + "&cver=10&man=" + config.getActionName()
                + "&l=testlayout&queueittoken=ts_sasa~cv_adsasa~ce_falwwwse~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895&"));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie") == null);
    }

    @Test
    public void ValidateRequest_InvalidCookie_InValidToken() throws Exception {
        QueueEventConfig config = new QueueEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);
        config.setLayoutName("testlayout");
        config.setVersion(10);
        config.setActionName("QueueAction");
        config.setCookieDomain("testDomain");

        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                return new StateInfo(true, false, null, null, null);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }
        };

        String targetUrl = "http://test.test.com/?b=h";
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl,
                "ts_sasa~cv_adsasa~ce_falwwwse~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertTrue(result.getRedirectUrl().startsWith("https://testDomain.com/error/hash/?c=testCustomer&e=e1&ver="
                + knownUserVersion + "&cver=10&man=" + config.getActionName()
                + "&l=testlayout&queueittoken=ts_sasa~cv_adsasa~ce_falwwwse~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895&"));
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
        assertTrue(conditions.get("cancelQueueCookie"));
    }

    @Test
    public void validateCancelRequest() throws Exception {
        CancelEventConfig config = new CancelEventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testDomain.com");
        config.setCookieDomain("testdomain");
        config.setVersion(10);
        config.setActionName("Queue Action (._~-) &!*|'\"");

        final HashMap<String, String> conditions = new HashMap<String, String>();

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                    String redirectType, String customerSecretKey) throws Exception {
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                    boolean validateTime) {
                if (!validateTime) {
                    return new StateInfo(true, true, "queueId", null, "queue");
                } else {
                    return new StateInfo(false, false, null, null, null);
                }
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                    String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                               // choose Tools | Templates.
            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookieWasCalled", "eventId:" + eventId + ",cookieDomain:" + cookieDomain);
            }
        };
        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedMan = "Queue%20Action%20%28._%7E-%29%20%26%21%2A%7C%27%22";
        String expectedUrl = "https://testDomain.com/cancel/testCustomer/e1/?c=testCustomer&e=e1" + "&ver="
                + knownUserVersion + "&cver=10" + "&man=" + expectedMan + "&r=url";

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateCancelRequest("url", config, "testCustomer", "key");

        assertTrue("eventId:e1,cookieDomain:testdomain".equals(conditions.get("cancelQueueCookieWasCalled")));
        assertTrue(result.doRedirect());
        assertTrue("queueId".equals(result.getQueueId()));
        String expUrl = expectedUrl.toLowerCase();
        String rdrUrl = result.getRedirectUrl().toLowerCase();
        assertTrue(expUrl.equals(rdrUrl));
        assertTrue(config.getEventId().equals(result.getEventId()));
        assertEquals(result.getActionName(), config.getActionName());
    }

    @Test
    public void getIgnoreRequest() throws Exception {
        UserInQueueService testObject = new UserInQueueService(null);
        RequestValidationResult result = testObject.getIgnoreActionResult("TestIgnoreAction");
        assertTrue(ActionType.IGNORE_ACTION.equals(result.getActionType()));
        assertFalse(result.doRedirect());
        assertNull(result.getEventId());
        assertNull(result.getQueueId());
        assertNull(result.getRedirectUrl());
        assertEquals(result.getActionName(), "TestIgnoreAction");
    }

    public static class QueueITTokenGenerator {

        public static String generateToken(Date timeStamp, String eventId, boolean extendableCookie,
                Integer cookieValidityMinute, String secretKey, String redirectType) {

            ArrayList<String> paramList = new ArrayList<String>();

            paramList.add(QueueParameterHelper.TimeStampKey + QueueParameterHelper.KeyValueSeparatorChar
                    + GetUnixTimestamp(timeStamp));
            if (cookieValidityMinute != null) {
                paramList.add(QueueParameterHelper.CookieValidityMinutesKey + QueueParameterHelper.KeyValueSeparatorChar
                        + cookieValidityMinute);
            }
            paramList.add(QueueParameterHelper.EventIdKey + QueueParameterHelper.KeyValueSeparatorChar + eventId);
            paramList.add(QueueParameterHelper.ExtendableCookieKey + QueueParameterHelper.KeyValueSeparatorChar
                    + extendableCookie);
            paramList.add(
                    QueueParameterHelper.RedirectTypeKey + QueueParameterHelper.KeyValueSeparatorChar + redirectType);

            String tokenWithoutHash = Utils.join(QueueParameterHelper.KeyValueSeparatorGroupChar, paramList);
            String hash = HashHelper.generateSHA256Hash(secretKey, tokenWithoutHash);
            String token = tokenWithoutHash + QueueParameterHelper.KeyValueSeparatorGroupChar
                    + QueueParameterHelper.HashKey + QueueParameterHelper.KeyValueSeparatorChar + hash;

            return token;
        }

        private static String GetUnixTimestamp(Date dateTime) {
            long totalSeconds = dateTime.getTime() / 1000;
            return Long.toString(totalSeconds);
        }
    }
}
