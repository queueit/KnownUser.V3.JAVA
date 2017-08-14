
package queueit.knownuserv3.sdk.integrationconfig;

import static org.junit.Assert.assertFalse;
import org.junit.Test;


public class UserAgentValidatorHelperTest {
      @Test
    public void Evaluate_Test() {
        
            TriggerPart triggerPart = new TriggerPart();
            triggerPart.Operator = ComparisonOperatorType.CONTAINS;
            triggerPart.ValueToCompare = "googlebot";
            assertFalse(UserAgentValidatorHelper.evaluate(triggerPart, "Googlebot sample useraagent"));

            triggerPart.ValueToCompare = "googlebot";
            triggerPart.Operator = ComparisonOperatorType.EQUALS;
            triggerPart.IsIgnoreCase = true;
            triggerPart.IsNegative = true;
            assert(UserAgentValidatorHelper.evaluate(triggerPart, "oglebot sample useraagent"));

            
            triggerPart.ValueToCompare = "googlebot";
            triggerPart.Operator = ComparisonOperatorType.CONTAINS;
            triggerPart.IsIgnoreCase = false;
            triggerPart.IsNegative = true;
            assertFalse(UserAgentValidatorHelper.evaluate(triggerPart, "googlebot"));


            triggerPart.ValueToCompare = "googlebot";
            triggerPart.IsIgnoreCase = true;
            triggerPart.IsNegative = false;
            triggerPart.Operator = ComparisonOperatorType.CONTAINS;
            assert (UserAgentValidatorHelper.evaluate(triggerPart, "Googlebot"));
    }
}
