package com.queue_it.connector;

import static org.junit.Assert.*;

import org.junit.Test;

public class UrlParameterProviderTest {

    @Test
    public void TryExtractQueueParams_Test() {
        String queueitToken = "ts_1480593661~cv_10~ce_false~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895~c_customerid~e_eventid~rt_disabled~h_218b734e-d5be-4b60-ad66-9b1b326266e2";

        QueueUrlParams queueParameter = QueueParameterHelper.extractQueueParams(queueitToken);
        assert queueParameter != null;
        assertEquals(1480593661, queueParameter.getTimeStamp());
        assertEquals("eventid", queueParameter.getEventId());
        assertEquals(10, (int) queueParameter.getCookieValidityMinutes());
        assertFalse(queueParameter.getExtendableCookie());
        assertEquals("944c1f44-60dd-4e37-aabc-f3e4bb1c8895", queueParameter.getQueueId());
        assertEquals("218b734e-d5be-4b60-ad66-9b1b326266e2", queueParameter.getHashCode());
        assertEquals(queueitToken, queueParameter.getQueueITToken());
        assertEquals("ts_1480593661~cv_10~ce_false~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895~c_customerid~e_eventid~rt_disabled", queueParameter.getQueueITTokenWithoutHash());
    }

    @Test
    public void TryExtractQueueParams_NotValidFormat_Test() {
        String queueitToken = "ts_sasa~cv_adsasa~ce_falwwwse~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895~h_218b734e-d5be-4b60-ad66-9b1b326266e2";
        String queueitTokenWithoutHash = "ts_sasa~cv_adsasa~ce_falwwwse~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895";

        QueueUrlParams queueParameter = QueueParameterHelper.extractQueueParams(queueitToken);
        assert queueParameter != null;
        assertEquals(0, queueParameter.getTimeStamp());
        assertEquals("", queueParameter.getEventId());
        assertNull(queueParameter.getCookieValidityMinutes());
        assertFalse(queueParameter.getExtendableCookie());
        assertEquals("944c1f44-60dd-4e37-aabc-f3e4bb1c8895", queueParameter.getQueueId());
        assertEquals("218b734e-d5be-4b60-ad66-9b1b326266e2", queueParameter.getHashCode());
        assertEquals(queueitToken, queueParameter.getQueueITToken());
        assertEquals(queueitTokenWithoutHash, queueParameter.getQueueITTokenWithoutHash());
    }

    @Test
    public void TryExtractQueueParams_Using_QueueitToken_With_No_Values_Test() {
        String queueitToken = "e~q~ts~ce~rt~h";

        QueueUrlParams queueParameter = QueueParameterHelper.extractQueueParams(queueitToken);
        assert queueParameter != null;
        assertEquals(0, queueParameter.getTimeStamp());
        assertEquals("", queueParameter.getEventId());
        assertNull(queueParameter.getCookieValidityMinutes());
        assertFalse(queueParameter.getExtendableCookie());
        assertEquals("", queueParameter.getQueueId());
        assertEquals("", queueParameter.getHashCode());
        assertEquals(queueitToken, queueParameter.getQueueITToken());
        assertEquals(queueitToken, queueParameter.getQueueITTokenWithoutHash());
    }

    @Test
    public void TryExtractQueueParams_Using_No_QueueitToken_Expect_Null() {
        QueueUrlParams queueParameter = QueueParameterHelper.extractQueueParams("");
        assertNull(queueParameter);
    }
}
