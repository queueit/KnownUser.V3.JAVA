package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.*;

import org.junit.Test;

public class UrlValidatorHelperTest {

    @Test
    public void Evaluate_Test() {
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.UrlPart = UrlPartType.PAGE_URL;
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "http://test.tesdomain.com:8080/test?q=1";
        assertFalse(UrlValidatorHelper.evaluate(triggerPart, "http://test.tesdomain.com:8080/test?q=2"));

        triggerPart.UrlPart = UrlPartType.PAGE_PATH;
        triggerPart.Operator = ComparisonOperatorType.EQUALS;
        triggerPart.ValueToCompare = "/Test/t1";
        triggerPart.IsIgnoreCase = true;
        assertTrue(UrlValidatorHelper.evaluate(triggerPart, "http://test.tesdomain.com:8080/test/t1?q=2&y02"));

        triggerPart.UrlPart = UrlPartType.HOST_NAME;
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "test.tesdomain.com";
        assertTrue(UrlValidatorHelper.evaluate(triggerPart, "http://m.test.tesdomain.com:8080/test?q=2"));

        triggerPart.UrlPart = UrlPartType.HOST_NAME;
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "test.tesdomain.com";
        triggerPart.IsNegative = true;
        assertFalse(UrlValidatorHelper.evaluate(triggerPart, "http://m.test.tesdomain.com:8080/test?q=2"));
    }
}
