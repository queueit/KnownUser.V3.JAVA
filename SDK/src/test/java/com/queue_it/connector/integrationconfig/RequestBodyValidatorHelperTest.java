package com.queue_it.connector.integrationconfig;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.queue_it.connector.KnownUserRequestWrapperMock;

public class RequestBodyValidatorHelperTest {
    @Test
    public void Evaluate_Test() {
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart.ValueToCompare = "test body";
        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        KnownUserRequestWrapperMock wrappedRequest = new KnownUserRequestWrapperMock(requestMock);

        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest));
        
        wrappedRequest.SetRequestBodyAsString("test body");

        assertTrue(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest));

        triggerPart.ValueToCompare = "ZZZ";
        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        assertTrue(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest));

        triggerPart.ValueToCompare = "Test";
        triggerPart.IsIgnoreCase = true;
        triggerPart.IsNegative = true;
        assertFalse(RequestBodyValidatorHelper.evaluate(triggerPart, wrappedRequest));
    }
}
