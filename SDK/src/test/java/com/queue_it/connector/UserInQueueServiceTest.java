
package com.queue_it.connector;

import static org.junit.Assert.*;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes,
                              String cookieDomainString, String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateQueueRequest("url", "token", config, "testCustomer", "key");
        assertFalse(result.doRedirect());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals("e1", result.getEventId());
        assertEquals("queueId", result.getQueueId());
        assertEquals("queue", result.getRedirectType());
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
        final HashMap<String, HashMap<String, Object>> callInfo = new HashMap<>();
        callInfo.put("firstCall", new HashMap<>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
                HashMap<String, Object> info = new HashMap<>();
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
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                                           String secretKey) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateQueueRequest("url", "token", config, "testCustomer", "key");
        assertFalse(result.doRedirect());
        assertEquals("queueId", callInfo.get("firstCall").get("queueId"));
        assertEquals("e1", callInfo.get("firstCall").get("eventId"));
        assertEquals("queue", callInfo.get("firstCall").get("redirectType"));
        assertEquals("key", callInfo.get("firstCall").get("customerSecretKey"));
        assertEquals(".testdomain.com", callInfo.get("firstCall").get("cookieDomain"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
                conditions.put("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, int cookieValidityMinutes, String customerSecretKey,
                                      boolean validateTime) {
                return new StateInfo(true, true, "queueId", "3", "idle");
            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                                           String secretKey) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateQueueRequest("url", "token", config, "testCustomer", "key");
        assertFalse(result.doRedirect());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals("e1", result.getEventId());
        assertEquals("queueId", result.getQueueId());
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookieWasCalled", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        assertEquals(redirectUrl.toUpperCase(), expectedErrorUrl.toUpperCase());
        assertEquals(config.getEventId(), result.getEventId());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(conditions.get("cancelQueueCookieWasCalled"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        assertEquals(redirectUrl.toUpperCase(), expectedErrorUrl.toUpperCase());
        assertEquals(config.getEventId(), result.getEventId());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(conditions.get("cancelQueueCookie"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        assertEquals(redirectUrl.toUpperCase(), expectedErrorUrl.toUpperCase());
        assertEquals(config.getEventId(), result.getEventId());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(conditions.get("cancelQueueCookie"));
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

        final HashMap<String, HashMap<String, Object>> callInfo = new HashMap<>();
        callInfo.put("firstCall", new HashMap<>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
                HashMap<String, Object> info = new HashMap<>();
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
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("eventId", eventId);
                obj.put("cookieDomain", cookieDomain);
                callInfo.put("cancelQueueCookieWasCalled", obj);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                                           String secretKey) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 60 * 60);
        String queueitToken = QueueITTokenGenerator.generateToken(date, "e1", true, null, customerKey, "queue");

        String targetUrl = "http://test.test.com?b=h";

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertFalse(result.doRedirect());
        assertEquals(callInfo.get("firstCall").get("eventId"), config.getEventId());
        assertNull(callInfo.get("firstCall").get("fixedCookieValidityMinutes"));
        assertEquals(callInfo.get("firstCall").get("cookieDomain"), config.getCookieDomain());
        assertEquals("queue", callInfo.get("firstCall").get("redirectType"));
        assertEquals(callInfo.get("firstCall").get("customerSecretKey"), customerKey);
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(callInfo.get("cancelQueueCookieWasCalled"));
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

        final HashMap<String, HashMap<String, Object>> callInfo = new HashMap<>();
        callInfo.put("firstCall", new HashMap<>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
                HashMap<String, Object> info = new HashMap<>();
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
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("eventId", eventId);
                obj.put("cookieDomain", cookieDomain);
                callInfo.put("cancelQueueCookieWasCalled", obj);
            }

            @Override
            public void reissueQueueCookie(String eventId, int cookieValidityMinutes, String cookieDomain,
                                           String secretKey) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        String queueitToken = "e_eventid~q_f8757c2d-34c2-4639-bef2-1736cdd30bbb~ri_34678c2d-34c2-4639-bef2-1736cdd30bbb~ts_1797033600~ce_False~cv_3~rt_DirectLink~h_5ee2babc3ac9fae9d80d5e64675710c371876386e77209f771007dc3e093e326";
        String targetUrl = "http://test.test.com?b=h";

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(targetUrl, queueitToken, config,
                "testCustomer", customerKey);
        assertFalse(result.doRedirect());
        assertEquals(callInfo.get("firstCall").get("eventId"), config.getEventId());
        assertEquals("DirectLink", callInfo.get("firstCall").get("redirectType"));
        assertEquals(callInfo.get("firstCall").get("cookieDomain"), config.getCookieDomain());
        assertEquals(3, callInfo.get("firstCall").get("fixedCookieValidityMinutes"));
        assertEquals(callInfo.get("firstCall").get("customerSecretKey"), customerKey);
        assertEquals(config.getEventId(), result.getEventId());
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(callInfo.get("cancelQueueCookieWasCalled"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        assertEquals(result.getRedirectUrl().toUpperCase(), expectedErrorUrl.toUpperCase());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(config.getEventId(), result.getEventId());
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(conditions.get("cancelQueueCookie"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/?c=testCustomer&e=e1" + "&ver=" + knownUserVersion
                + "&cver=10" + "&man=" + config.getActionName() + "&l=" + config.getLayoutName();

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(null, "", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertEquals(result.getRedirectUrl().toUpperCase(), expectedErrorUrl.toUpperCase());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(config.getEventId(), result.getEventId());
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(conditions.get("cancelQueueCookie"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        String knownUserVersion = UserInQueueService.SDK_VERSION;
        String expectedErrorUrl = "https://testDomain.com/?c=testCustomer&e=e1" + "&ver=" + knownUserVersion
                + "&cver=10" + "&man=" + config.getActionName() + "&l=" + config.getLayoutName();

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);
        RequestValidationResult result = testObject.validateQueueRequest(null, "", config, "testCustomer", "key");
        assertTrue(result.doRedirect());
        assertEquals(result.getRedirectUrl().toUpperCase(), expectedErrorUrl.toUpperCase());
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(config.getEventId(), result.getEventId());
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(config.getEventId(), result.getEventId());
        assertEquals(result.getActionName(), config.getActionName());
        assertNull(conditions.get("cancelQueueCookie"));
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

        final HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                conditions.put("cancelQueueCookie", true);
            }

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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
        assertFalse(conditions.get("isStoreWasCalled"));
        assertEquals(config.getEventId(), result.getEventId());
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

        final HashMap<String, String> conditions = new HashMap<>();

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {

            @Override
            public void store(String eventId, String queueId, Integer fixedCookieValidityMinutes, String cookieDomain,
                              String redirectType, String customerSecretKey) {
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
                throw new UnsupportedOperationException("Not supported yet.");
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

        assertEquals("eventId:e1,cookieDomain:testdomain", conditions.get("cancelQueueCookieWasCalled"));
        assertTrue(result.doRedirect());
        assertEquals("queueId", result.getQueueId());
        String expUrl = expectedUrl.toLowerCase();
        String rdrUrl = result.getRedirectUrl().toLowerCase();
        assertEquals(expUrl, rdrUrl);
        assertEquals(config.getEventId(), result.getEventId());
        assertEquals(result.getActionName(), config.getActionName());
    }

    @Test
    public void getIgnoreRequest() {
        UserInQueueService testObject = new UserInQueueService(null);
        RequestValidationResult result = testObject.getIgnoreActionResult("TestIgnoreAction");
        assertEquals(ActionType.IGNORE_ACTION, result.getActionType());
        assertFalse(result.doRedirect());
        assertNull(result.getEventId());
        assertNull(result.getQueueId());
        assertNull(result.getRedirectUrl());
        assertEquals(result.getActionName(), "TestIgnoreAction");
    }

    public static class QueueITTokenGenerator {

        public static String generateToken(Date timeStamp, String eventId, boolean extendableCookie,
                                           Integer cookieValidityMinute, String secretKey, String redirectType) {

            ArrayList<String> paramList = new ArrayList<>();

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

            return tokenWithoutHash + QueueParameterHelper.KeyValueSeparatorGroupChar
                    + QueueParameterHelper.HashKey + QueueParameterHelper.KeyValueSeparatorChar + hash;
        }

        private static String GetUnixTimestamp(Date dateTime) {
            long totalSeconds = dateTime.getTime() / 1000;
            return Long.toString(totalSeconds);
        }
    }
}
