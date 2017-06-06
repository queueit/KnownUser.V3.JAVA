package queueit.knownuserv3.sdk;

import java.util.ArrayList;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import queueit.knownuserv3.sdk.integrationconfig.CustomerIntegration;
import queueit.knownuserv3.sdk.integrationconfig.IntegrationConfigModel;
import queueit.knownuserv3.sdk.integrationconfig.TriggerModel;
import queueit.knownuserv3.sdk.integrationconfig.TriggerPart;

public class KnownUserTest {

    class UserInQueueServiceMock implements IUserInQueueService {

        public ArrayList<ArrayList<String>> validateRequestCalls = new ArrayList<>();
        public ArrayList<ArrayList<String>> cancelQueueCookieCalls = new ArrayList<>();
        public ArrayList<ArrayList<String>> extendQueueCookieCalls = new ArrayList<>();

        @Override
        public RequestValidationResult validateRequest(String targetUrl, String queueitToken, EventConfig config, String customerId, String secretKey) throws Exception {
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
            validateRequestCalls.add(args);

            return null;
        }

        @Override
        public void cancelQueueCookie(String eventId, String cookieDomain) {
            ArrayList<String> args = new ArrayList<>();
            args.add(eventId);
            args.add(cookieDomain);
            cancelQueueCookieCalls.add(args);
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
    }

    @Test
    public void cancelQueueCookieTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        // Act
        KnownUser.cancelQueueCookie("eventId", "cookieDomain", null, null);

        // Assert
        assertTrue("eventId".equals(mock.cancelQueueCookieCalls.get(0).get(0)));
        assertTrue("cookieDomain".equals(mock.cancelQueueCookieCalls.get(0).get(1)));
    }

    @Test
    public void cancelQueueCookieNullEventIdTest() {
        // Arrange        
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelQueueCookie(null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "eventId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.cancelQueueCookieCalls.isEmpty());
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
    public void validateRequestByLocalEventConfigNullCustomerIdTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", null, null, null, null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "customerId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByLocalEventConfigNullSecretKeyTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", null, "customerId", null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByLocalEventConfigNullEventConfigTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", null, "customerId", null, null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "eventConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByLocalEventConfigNullEventIdTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        EventConfig eventConfig = new EventConfig();
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
            KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", eventConfig, "customerId", null, null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "EventId from eventConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByLocalEventConfigNullQueueDomainTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        EventConfig eventConfig = new EventConfig();
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
            KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", eventConfig, "customerId", null, null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "QueueDomain from eventConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByLocalEventConfigInvalidCookieValidityMinuteTest() {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);
        boolean exceptionWasThrown = false;

        EventConfig eventConfig = new EventConfig();
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
            KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", eventConfig, "customerId", null, null, "secretKey");
        } catch (Exception ex) {
            exceptionWasThrown = "cookieValidityMinute from eventConfig should be greater than 0.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByLocalEventConfigTest() throws Exception {
        // Arrange
        UserInQueueServiceMock mock = new UserInQueueServiceMock();
        KnownUser.setUserInQueueService(mock);

        EventConfig eventConfig = new EventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        KnownUser.validateRequestByLocalEventConfig("targetUrl", "queueitToken", eventConfig, "customerId", null, null, "secretKey");

        // Assert
        assertTrue("targetUrl".equals(mock.validateRequestCalls.get(0).get(0)));
        assertTrue("queueitToken".equals(mock.validateRequestCalls.get(0).get(1)));
        assertTrue("cookieDomain:layoutName:culture:eventId:queueDomain:true:10:12".equals(mock.validateRequestCalls.get(0).get(2)));
        assertTrue("customerId".equals(mock.validateRequestCalls.get(0).get(3)));
        assertTrue("secretKey".equals(mock.validateRequestCalls.get(0).get(4)));
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
        assertTrue(mock.validateRequestCalls.isEmpty());
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
            KnownUser.validateRequestByIntegrationConfig("currentUrl", "queueitToken", null, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "customerIntegrationInfo can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByIntegrationConfigTest() throws Exception {
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
        //config.ActionType = "Queue";
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

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", null, null, "secretKey");

        // Assert
        assertTrue(mock.validateRequestCalls.size() == 1);
        assertTrue("http://test.com?event1=true".equals(mock.validateRequestCalls.get(0).get(0)));
        assertTrue("queueitToken".equals(mock.validateRequestCalls.get(0).get(1)));
        assertTrue(".test.com:Christmas Layout by Queue-it:da-DK:event1:knownusertest.queue-it.net:true:20:3".equals(mock.validateRequestCalls.get(0).get(2)));
        assertTrue("customerId".equals(mock.validateRequestCalls.get(0).get(3)));
        assertTrue("secretKey".equals(mock.validateRequestCalls.get(0).get(4)));
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
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", null, null, "secretKey");

        // Assert
        assertTrue(mock.validateRequestCalls.isEmpty());
        assertTrue(!result.doRedirect());
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
        //config.ActionType = "Queue";
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

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", null, null, "secretKey");

        // Assert
        assertTrue(mock.validateRequestCalls.size() == 1);
        assertTrue("http://forcedtargeturl.com".equals(mock.validateRequestCalls.get(0).get(0)));
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
        //config.ActionType = "Queue";
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

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", null, null, "secretKey");

        // Assert
        assertTrue(mock.validateRequestCalls.size() == 1);
        assertTrue("http://forcedtargeturl.com".equals(mock.validateRequestCalls.get(0).get(0)));
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
        //config.ActionType = "Queue";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[]{trigger};
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "EventTargetUrl";

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[]{config};
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration, "customerId", null, null, "secretKey");

        // Assert
        assertTrue(mock.validateRequestCalls.size() == 1);
        assertTrue("".equals(mock.validateRequestCalls.get(0).get(0)));
    }
}
