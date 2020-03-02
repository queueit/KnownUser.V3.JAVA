package queueit.knownuserv3.sdk.integrationconfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class HttpHeaderHelperTest {

    @Test
    public void Evaluate_Test() {
        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.Headers.put("MyHeaderName", "MyHeaderValue");
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.HttpHeaderName = "MyHeaderName";

        triggerPart.ValueToCompare = "MyHeaderValue";
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = false;
        assertTrue(HttpHeaderValidatorHelper.evaluate(triggerPart, httpContextMock));

        triggerPart.ValueToCompare = "Value";
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = false;
        assertTrue(HttpHeaderValidatorHelper.evaluate(triggerPart, httpContextMock));

        triggerPart.ValueToCompare = "MyHeaderValue";
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.IsNegative = true;
        triggerPart.IsIgnoreCase = false;
        assertFalse(HttpHeaderValidatorHelper.evaluate(triggerPart, httpContextMock));

        triggerPart.ValueToCompare = "myheadervalue";
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;
        assertTrue(HttpHeaderValidatorHelper.evaluate(triggerPart, httpContextMock));
    }
}
