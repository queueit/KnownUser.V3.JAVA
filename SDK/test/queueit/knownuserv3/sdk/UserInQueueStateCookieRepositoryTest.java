/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queueit.knownuserv3.sdk;

import java.util.HashMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author MOSA
 */
public class UserInQueueStateCookieRepositoryTest {

    @Test
    public void store_getState_ExtendableCookie_CookieIsSaved() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;
        HashMap<String, HashMap<String, Object>> cookies = new HashMap<>();
        cookies.put(cookieKey, new HashMap<>());

        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                HashMap<String, Object> cookie = cookies.get(cookieName);
                cookie.put("cookieValue", cookieValue);
                cookie.put("cookieValue", cookieValue);
                cookie.put("expiration", expiration);
                cookie.put("cookieDomain", cookieDomain);
            }

            @Override
            public String getCookie(String cookieName) {
                return String.valueOf(cookies.get(cookieName).get("cookieValue"));
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, true, cookieDomain, cookieValidity, secretKey);
        StateInfo state = testObject.getState(eventId, secretKey);

        assertTrue(state.isValid());
        assertTrue(state.getQueueId().equals(queueId));
        assertTrue(state.isStateExtendable());
        assertTrue(Math.abs(System.currentTimeMillis() / 1000L + 10 * 60 - state.getExpires()) < 100);
        assertTrue((int) cookies.get(cookieKey).get("expiration") == 24 * 60 * 60);
        assertTrue(cookies.get(cookieKey).get("cookieDomain").equals(cookieDomain));

    }

    @Test
    public void store_getState_TamperedCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;
        HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, false, cookieDomain, cookieValidity, secretKey);

        StateInfo state = testObject.getState(eventId, secretKey);
        assertTrue(state.isValid());

        String cookieString = cookies.get(cookieKey);
        cookieString = cookieString.replace("IsCookieExtendable=false", "IsCookieExtendable=true");
        cookies.replace(cookieKey, cookieString);
        state = testObject.getState(eventId, secretKey);
        assertFalse(state.isValid());

    }

    @Test
    public void store_getState_ExpiredCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, false, cookieDomain, -1, secretKey);

        StateInfo state = testObject.getState(eventId, secretKey);
        assertFalse(state.isValid());

    }

    @Test
    public void store_getState_DifferentEventId_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);

        HashMap<String, String> cookies = new HashMap<>();

        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                cookies.put(cookieName, cookieValue);
            }

            @Override
            public String getCookie(String cookieName) {
                return cookies.get(cookieName);
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, false, cookieDomain, 10, secretKey);
        StateInfo state = testObject.getState(eventId, secretKey);
        assertTrue(state.isValid());

        state = testObject.getState("event2", secretKey);
        assertFalse(state.isValid());

    }

    @Test
    public void store_getState_InvalidCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {

            }

            @Override
            public String getCookie(String cookieName) {
                return "IsCookieExtendable=ooOOO&Expires=|||&QueueId=000&Hash=23232$$$";
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, false, cookieDomain, 10, secretKey);
        StateInfo state = testObject.getState(eventId, secretKey);
        assertFalse(state.isValid());

    }

    @Test
    public void cancelQueueCookie_Test() throws Exception {
        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        HashMap<String, HashMap<String, Object>> cookies = new HashMap<>();
        cookies.put(cookieKey + "1", new HashMap<>());
        cookies.put(cookieKey + "2", new HashMap<>());

        ICookieManager cookieManager = new ICookieManager() {
            public int setCookieCallNumber = 0;

            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                setCookieCallNumber++;
                HashMap<String, Object> cookie = cookies.get(cookieName + String.valueOf(setCookieCallNumber));
                cookie.put("cookieValue", cookieValue);
                cookie.put("cookieValue", cookieValue);
                cookie.put("expiration", expiration);
                cookie.put("cookieDomain", cookieDomain);

            }

            @Override
            public String getCookie(String cookieName) {
                return String.valueOf(cookies.get(cookieName + String.valueOf(setCookieCallNumber)).get("cookieValue"));
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, true, "cookieDomain", 10, secretKey);
        assertTrue(testObject.getState(eventId, secretKey).isValid());

        testObject.cancelQueueCookie(eventId, "cookieDomain");

        assertTrue((int) cookies.get(cookieKey + "2").get("expiration") == 0);
        assertTrue(cookies.get(cookieKey + "2").get("cookieValue") == null);
        assertTrue(cookies.get(cookieKey + "2").get("cookieDomain").equals("cookieDomain"));
        assertFalse(testObject.getState(eventId, secretKey).isValid());

    }

    @Test
    public void cancelQueueCookie_CookieDoesNotExist_Test() {
        String eventId = "event1";
        HashMap<String, Object> conditions = new HashMap<>();
        conditions.put("isSetCookieCalled", false);
        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                conditions.replace("isSetCookieCalled", true);
            }

            @Override
            public String getCookie(String cookieName) {
                return null;
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.cancelQueueCookie(eventId, "cookieDomain");

        assertFalse(Boolean.valueOf(String.valueOf(conditions.get("isSetCookieCalled"))));
    }

    @Test
    public void extendQueueCookie_CookietExist_Test() throws Exception {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        HashMap<String, HashMap<String, Object>> cookies = new HashMap<>();
        cookies.put(cookieKey + "1", new HashMap<>());
        cookies.put(cookieKey + "2", new HashMap<>());

        ICookieManager cookieManager = new ICookieManager() {
            public int setCookieCallNumber = 0;

            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                setCookieCallNumber++;
                HashMap<String, Object> cookie = cookies.get(cookieName + String.valueOf(setCookieCallNumber));
                cookie.put("cookieValue", cookieValue);
                cookie.put("cookieValue", cookieValue);
                cookie.put("expiration", expiration);
                cookie.put("cookieDomain", cookieDomain);

            }

            @Override
            public String getCookie(String cookieName) {
                return String.valueOf(cookies.get(cookieName + String.valueOf(setCookieCallNumber)).get("cookieValue"));
            }
        };

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.store(eventId, queueId, true, "cookieDomain", 10, secretKey);
        assertTrue(testObject.getState(eventId, secretKey).isValid());

        testObject.extendQueueCookie(eventId, 12, "cookieDomain", secretKey);

        StateInfo state = testObject.getState(eventId, secretKey);

        assertTrue(state.isValid());
        assertTrue(state.getQueueId().equals(queueId));
        assertTrue(state.isStateExtendable());
        assertTrue(Math.abs(System.currentTimeMillis() / 1000L + 12 * 60 - state.getExpires()) < 100);
        assertTrue((int) cookies.get(cookieKey + "2").get("expiration") == 24 * 60 * 60);
        assertTrue(cookies.get(cookieKey + "2").get("cookieDomain").equals("cookieDomain"));
    }

    @Test
    public void extendQueueCookie_CookieDoesNotExist_Test() {

        String eventId = "event1";
        String secretKey = "secretKey";
        HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isSetCookieCalled", false);

        ICookieManager cookieManager = new ICookieManager() {
            @Override
            public void setCookie(String cookieName, String cookieValue, int expiration, String cookieDomain) {
                conditions.replace("isSetCookieCalled", true);
            }

            @Override
            public String getCookie(String cookieName) {
                return null;
            }
        };
        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(cookieManager);
        testObject.extendQueueCookie(eventId, 12, "queueDomain", secretKey);
        assertFalse(conditions.get("isSetCookieCalled"));
    }
}
