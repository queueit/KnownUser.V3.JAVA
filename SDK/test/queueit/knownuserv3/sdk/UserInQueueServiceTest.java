/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queueit.knownuserv3.sdk;

import java.util.HashMap;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author MOSA
 */
public class UserInQueueServiceTest {

    //ExtendableCookie Cookie
    @Test
    public void validateRequest_ValidState_ExtendableCookie_NoCookieExtensionFromConfig_DoNotRedirectDoNotStoreCookieWithExtension()
            throws Exception {

        EventConfig config = new EventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(false);

        HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {
            @Override
            public void store(String eventId, String queueId, boolean isStateExtendable, String cookieDomain, int cookieValidityMinute, String customerSecretKey) throws Exception {
                conditions.replace("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, String customerSecretKey) {
                return new StateInfo(true, "queueId", true, System.currentTimeMillis() / 1000L + 10 * 60);

            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void extendQueueCookie(String eventId, int cookieValidityMinute, String cookieDomain, String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateRequest("url", "token", config, "testCustomer", "key");
        assertTrue(!result.doRedirect());
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(result.getEventId().equals("e1"));
        assertTrue(result.getQueueId().equals("queueId"));

    }

    @Test
    public void validateRequest_ValidState_ExtendableCookie_CookieExtensionFromConfig_DoNotRedirectDoStoreCookieWithExtension() throws Exception {

        EventConfig config = new EventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(true);
        config.setCookieDomain(".testdomain.com");
        HashMap<String, HashMap<String, Object>> callInfo = new HashMap<>();
        callInfo.put("firstCall", new HashMap<>());
        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {
            @Override
            public void store(String eventId, String queueId, boolean isStateExtendable, String cookieDomain,
                    int cookieValidityMinute, String customerSecretKey) throws Exception {
                HashMap<String, Object> info = new HashMap<>();
                info.put("eventId", eventId);
                info.put("isStateExtendable", isStateExtendable);
                info.put("cookieDomain", cookieDomain);
                info.put("queueId", queueId);
                info.put("customerSecretKey", customerSecretKey);
                callInfo.put("firstCall", info);
            }

            @Override
            public StateInfo getState(String eventId, String customerSecretKey) {
                return new StateInfo(true, "queueId", true, System.currentTimeMillis() / 1000L + 10 * 60);

            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void extendQueueCookie(String eventId, int cookieValidityMinute, String cookieDomain, String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };

        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateRequest("url", "token", config, "testCustomer", "key");
        assertTrue(!result.doRedirect());
        assertTrue(callInfo.get("firstCall").get("queueId").equals("queueId"));
        assertTrue(callInfo.get("firstCall").get("eventId").equals("e1"));
        assertTrue(callInfo.get("firstCall").get("isStateExtendable").equals(true));
        assertTrue(callInfo.get("firstCall").get("customerSecretKey").equals("key"));
        assertTrue(callInfo.get("firstCall").get("cookieDomain").equals(".testdomain.com"));

    }

@Test

        public void validateRequest_ValidState_NoExtendableCookie_DoNotRedirectDoNotStoreCookieWithExtension()  
                throws Exception {

        EventConfig config = new EventConfig();
        config.setEventId("e1");
        config.setQueueDomain("testdomain.com");
        config.setCookieValidityMinute(10);
        config.setExtendCookieValidity(true);

        HashMap<String, Boolean> conditions = new HashMap<>();
        conditions.put("isStoreWasCalled", false);

        IUserInQueueStateRepository cookieProviderMock = new IUserInQueueStateRepository() {
            @Override
            public void store(String eventId, String queueId, boolean isStateExtendable, String cookieDomain, int cookieValidityMinute, String customerSecretKey) throws Exception {
                conditions.replace("isStoreWasCalled", true);
            }

            @Override
            public StateInfo getState(String eventId, String customerSecretKey) {
                return new StateInfo(true, "queueId",false, System.currentTimeMillis() / 1000L + 10 * 60);

            }

            @Override
            public void cancelQueueCookie(String eventId, String cookieDomain) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void extendQueueCookie(String eventId, int cookieValidityMinute, String cookieDomain, String secretKey) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        UserInQueueService testObject = new UserInQueueService(cookieProviderMock);

        RequestValidationResult result = testObject.validateRequest("url", "token", config, "testCustomer", "key");
        assertTrue(!result.doRedirect());
        assertTrue(!conditions.get("isStoreWasCalled"));
        assertTrue(result.getEventId().equals("e1"));
        assertTrue(result.getQueueId().equals("queueId"));
        }
}
