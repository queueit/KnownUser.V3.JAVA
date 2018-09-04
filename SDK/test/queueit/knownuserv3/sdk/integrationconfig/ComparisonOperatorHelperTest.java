package queueit.knownuserv3.sdk.integrationconfig;

import static org.junit.Assert.*;
import org.junit.Test;

public class ComparisonOperatorHelperTest {

    @Test
    public void Evaluate_Equals() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS, false, false, "test1", "test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS, false, false, "test1", "Test1", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS, false, true, "test1", "Test1", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS, true, false, "test1", "Test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS, true, false, "test1", "test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS, true, true, "test1", "Test1", null));
    }

    @Test
    public void Evaluate_Contains() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, false, false, "test_test1_test", "test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, false, false, "test_test1_test", "Test1", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, false, true, "test_test1_test", "Test1", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, true, false, "test_test1_test", "Test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, true, true, "test_test1", "Test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, true, false, "test_test1", "test1", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS, false, false, "test_dsdsdsdtest1", "*", null));
    }

    @Test
    public void Evaluate_StartsWith() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.STARTS_WITH, false, false, "test1_test1_test", "test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.STARTS_WITH, false, false, "test1_test1_test", "Test1", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.STARTS_WITH, false, true, "test1_test1_test", "Test1", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.STARTS_WITH, true, true, "test1_test1_test", "Test1", null));
    }

    @Test
    public void Evaluate_EndsWith() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.ENDS_WITH, false, false, "test1_test1_testshop", "shop", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.ENDS_WITH, false, false, "test1_test1_testshop2", "shop", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.ENDS_WITH, false, true, "test1_test1_testshop", "Shop", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.ENDS_WITH, true, true, "test1_test1_testshop", "Shop", null));
    }

    @Test
    public void Evaluate_MatchesWith() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.MATCHES_WITH, false, false, "test1_test1_testshop", ".*shop.*", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.MATCHES_WITH, false, false, "test1_test1_testshop2", ".*Shop.*", null));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.MATCHES_WITH, false, true, "test1_test1_testshop", ".*Shop.*", null));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.MATCHES_WITH, true, true, "test1_test1_testshop", ".*Shop.*", null));
    }

    @Test
    public void Evaluate_EqualsAny() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS_ANY, false, false, "test1", null, new String[]{"test1"}));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS_ANY, false, false, "test1", null, new String[]{"Test1"}));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS_ANY, false, true, "test1", null, new String[]{"Test1"}));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS_ANY, true, false, "test1", null, new String[]{"Test1"}));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS_ANY, true, false, "test1", null, new String[]{"test1"}));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.EQUALS_ANY, true, true, "test1", null, new String[]{"Test1"}));
    }

    @Test
    public void Evaluate_ContainsAny() {
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, false, false, "test_test1_test", null, new String[]{"test1"}));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, false, false, "test_test1_test", null, new String[]{"Test1"}));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, false, true, "test_test1_test", null, new String[]{"Test1"}));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, true, false, "test_test1_test", null, new String[]{"Test1"}));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, true, true, "test_test1", null, new String[]{"Test1"}));
        assertFalse(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, true, false, "test_test1", null, new String[]{"test1"}));
        assertTrue(ComparisonOperatorHelper.evaluate(ComparisonOperatorType.CONTAINS_ANY, false, false, "test_dsdsdsdtest1", null, new String[]{"*"}));
    }
}
