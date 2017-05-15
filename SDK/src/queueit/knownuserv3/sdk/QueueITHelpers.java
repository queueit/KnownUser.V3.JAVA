package queueit.knownuserv3.sdk;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class Utils {

    public static boolean isNullOrWhiteSpace(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

class QueueParameterHelper {

    public static final String TIME_STAMP_KEY = "ts";
    public static final String EXTENDABLE_COOKIE_KEY = "ce";
    public static final String COOKIE_VALIDITY_MINUTE_KEY = "cv";
    public static final String HASH_KEY = "h";
    public static final String QUEUE_ID_KEY = "q";

    public static final String EVENT_ID_KEY = "e";
    public static final String KEY_VALUE_SEPARATOR_CHAR = "_";
    public static final String KEY_VALUE_SEPARATPR_GROUP_KEY = "~";

    public static QueueUrlParams extractQueueParams(String queueitToken) {
        try {
            if (Utils.isNullOrWhiteSpace(queueitToken)) {
                return null;
            }

            QueueUrlParams result = new QueueUrlParams();
            result.setQueueITToken(queueitToken);

            String[] paramList = queueitToken.split(KEY_VALUE_SEPARATPR_GROUP_KEY);
            for (String paramKeyValue : paramList) {
                String[] keyValueArr = paramKeyValue.split(KEY_VALUE_SEPARATOR_CHAR);

                switch (keyValueArr[0]) {
                    case TIME_STAMP_KEY:
                        if (Utils.isLong(keyValueArr[1])) {
                            result.setTimeStamp(Long.parseLong(keyValueArr[1]));
                        } else {
                            result.setTimeStamp(0);
                        }
                        break;
                    case COOKIE_VALIDITY_MINUTE_KEY: {
                        if (Utils.isInteger(keyValueArr[1])) {
                            result.setCookieValidityMinute(Integer.parseInt(keyValueArr[1]));
                        } else {
                            result.setCookieValidityMinute(null);
                        }
                        break;
                    }

                    case EVENT_ID_KEY:
                        result.setEventId(keyValueArr[1]);
                        break;
                    case QUEUE_ID_KEY:
                        result.setQueueId(keyValueArr[1]);
                        break;
                    case EXTENDABLE_COOKIE_KEY: {

                        result.setExtendableCookie(Boolean.parseBoolean(keyValueArr[1]));
                        break;
                    }
                    case HASH_KEY:
                        result.setHashCode(keyValueArr[1]);
                        break;

                }
            }
            String queueITTokenWithoutHash = result.getQueueITToken().replace(KEY_VALUE_SEPARATPR_GROUP_KEY + HASH_KEY + KEY_VALUE_SEPARATOR_CHAR + result.getHashCode(), "");
            result.setQueueITTokenWithoutHash(queueITTokenWithoutHash);
            return result;
        } catch (Exception ex) {
            return null;
        }
    }
}

class HashHelper {

    public static String generateSHA256Hash(String secretKey, String stringToHash) throws Exception {
        byte[] secretKeyBytes = secretKey.getBytes("UTF-8");
        SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] bytes = stringToHash.getBytes("UTF-8");
        byte[] rawHmac = mac.doFinal(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : rawHmac) {
            sb.append(String.format("%1$02x", b));
        }

        return sb.toString();
    }
}

class QueueUrlParams {

    private String eventId;
    private String hashCode;
    private boolean extendableCookie;
    private String queueITToken;
    private String queueITTokenWithoutHash;
    private Integer cookieValidityMinute;
    private long timeStamp;
    private String queueId;

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public boolean getExtendableCookie() {
        return this.extendableCookie;
    }

    public void setExtendableCookie(boolean extendableCookie) {
        this.extendableCookie = extendableCookie;
    }

    public Integer getCookieValidityMinute() {
        return this.cookieValidityMinute;
    }

    public void setCookieValidityMinute(Integer cookieValidityMinute) {
        this.cookieValidityMinute = cookieValidityMinute;
    }

    public String getQueueITToken() {
        return this.queueITToken;
    }

    public void setQueueITToken(String queueITToken) {
        this.queueITToken = queueITToken;
    }

    public String getQueueITTokenWithoutHash() {
        return this.queueITTokenWithoutHash;
    }

    public void setQueueITTokenWithoutHash(String queueITTokenWithoutHash) {
        this.queueITTokenWithoutHash = queueITTokenWithoutHash;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getQueueId() {
        return this.queueId;
    }
}
