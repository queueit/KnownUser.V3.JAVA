package queueit.knownuserv3.sdk.integrationconfig;

import static org.junit.Assert.*;
import org.junit.Test;
import javax.servlet.http.Cookie;

public class CookieValidatorHelperTest {

    @Test
    public void Evaluate_Test() {
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.CookieName = "c1";
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "1";

        Cookie[] cookieCollection = null;
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, cookieCollection));

        cookieCollection = new Cookie[3];
        cookieCollection[0] = new Cookie("c5", "5");
        cookieCollection[1] = new Cookie("c1", "1");
        cookieCollection[2] = new Cookie("c2", "test");
        assertTrue(CookieValidatorHelper.evaluate(triggerPart, cookieCollection));

        triggerPart.ValueToCompare = "5";
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, cookieCollection));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.CookieName = "c2";
        assertTrue(CookieValidatorHelper.evaluate(triggerPart, cookieCollection));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        triggerPart.CookieName = "c2";
        assertFalse(CookieValidatorHelper.evaluate(triggerPart, cookieCollection));
    }
}
