package com.queue_it.connector;

import java.util.AbstractCollection;
import java.util.Iterator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.Exception;
import java.net.URLEncoder;

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

    public static String join(String delimiter, AbstractCollection<String> s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        Iterator<String> iter = s.iterator();
        StringBuilder builder = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            builder.append(delimiter).append(iter.next());
        }
        return builder.toString();
    }

    public static String encodeUrl(String arg) {
        try {
            String value = URLEncoder.encode(arg, "UTF-8").replaceAll("\\+", "%20").replaceAll("&", "\\%26")
                    .replaceAll("!", "\\%21").replaceAll("'", "\\%27").replaceAll("\\(", "\\%28")
                    .replaceAll("~", "\\%7E").replaceAll("\\|", "\\%7C").replaceAll("\\)", "\\%29")
                    .replaceAll("\\*", "\\%2A");
            return value;
        } catch (Exception e) {
            return arg;
        }
    }
}

class ConnectorDiagnostics {
    public boolean isEnabled;
    public boolean hasError;
    public RequestValidationResult validationResult;

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean getHasError() {
        return this.hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public RequestValidationResult getValidationResult() {
        return this.validationResult;
    }

    public void setValidationResult(RequestValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    private void SetStateWithTokenError(String customerId, String errorCode) {
        hasError = true;
        String redirectUrl = "https://" + customerId + ".api2.queue-it.net/" + customerId
                + "/diagnostics/connector/error/?code=" + errorCode;

        validationResult = new RequestValidationResult("ConnectorDiagnosticsRedirect", null, null, redirectUrl, null,
                null);
    }

    private void SetStateWithSetupError() {
        hasError = true;
        validationResult = new RequestValidationResult("ConnectorDiagnosticsRedirect", null, null,
                "https://api2.queue-it.net/diagnostics/connector/error/?code=setup", null, null);
    }

    public static ConnectorDiagnostics Verify(String customerId, String secretKey, String queueitToken){

        ConnectorDiagnostics diagnostics = new ConnectorDiagnostics();

        QueueUrlParams qParams = QueueParameterHelper.extractQueueParams(queueitToken);

        if (qParams == null) {
            return diagnostics;
        }

        if (qParams.getRedirectType() == null) {
            return diagnostics;
        }

        if (!"debug".equals(qParams.getRedirectType())) {
            return diagnostics;
        }

        if (Utils.isNullOrWhiteSpace(customerId) || Utils.isNullOrWhiteSpace(secretKey)) {
            diagnostics.SetStateWithSetupError();
            return diagnostics;
        }

        String calculatedHash = HashHelper.generateSHA256Hash(secretKey, qParams.getQueueITTokenWithoutHash());

        if (!calculatedHash.toUpperCase().equals(qParams.getHashCode().toUpperCase())) {
            diagnostics.SetStateWithTokenError(customerId, "hash");
            return diagnostics;
        }

        if (qParams.getTimeStamp() < System.currentTimeMillis() / 1000L) {
            diagnostics.SetStateWithTokenError(customerId, "timestamp");
            return diagnostics;
        }

        diagnostics.isEnabled = true;

        return diagnostics;
    }
}

class QueueParameterHelper {

    public static final String TimeStampKey = "ts";
    public static final String ExtendableCookieKey = "ce";
    public static final String CookieValidityMinutesKey = "cv";
    public static final String HashKey = "h";
    public static final String QueueIdKey = "q";
    public static final String RedirectTypeKey = "rt";
    public static final String EventIdKey = "e";
    public static final String KeyValueSeparatorChar = "_";
    public static final String KeyValueSeparatorGroupChar = "~";

    public static QueueUrlParams extractQueueParams(String queueitToken) {

        if (Utils.isNullOrWhiteSpace(queueitToken)) {
            return null;
        }
        QueueUrlParams result = new QueueUrlParams();
        result.setQueueITToken(queueitToken);

        String[] paramList = queueitToken.split(KeyValueSeparatorGroupChar);
        for (String paramKeyValue : paramList) {
            String[] keyValueArr = paramKeyValue.split(KeyValueSeparatorChar);

            if (keyValueArr.length != 2)
                continue;

            if (HashKey.equals(keyValueArr[0])) {
                result.setHashCode(keyValueArr[1]);
            } else if (TimeStampKey.equals(keyValueArr[0])) {
                if (Utils.isLong(keyValueArr[1])) {
                    result.setTimeStamp(Long.parseLong(keyValueArr[1]));
                }
            } else if (CookieValidityMinutesKey.equals(keyValueArr[0])) {
                if (Utils.isInteger(keyValueArr[1])) {
                    result.setCookieValidityMinutes(Integer.parseInt(keyValueArr[1]));
                }
            } else if (EventIdKey.equals(keyValueArr[0])) {
                result.setEventId(keyValueArr[1]);
            } else if (QueueIdKey.equals(keyValueArr[0])) {
                result.setQueueId(keyValueArr[1]);
            } else if (ExtendableCookieKey.equals(keyValueArr[0])) {
                result.setExtendableCookie(Boolean.parseBoolean(keyValueArr[1]));
            } else if (RedirectTypeKey.equals(keyValueArr[0])) {
                result.setRedirectType(keyValueArr[1]);
            }
        }
        String queueITTokenWithoutHash = result.getQueueITToken()
                .replace(KeyValueSeparatorGroupChar + HashKey + KeyValueSeparatorChar + result.getHashCode(), "");
        result.setQueueITTokenWithoutHash(queueITTokenWithoutHash);
        return result;
    }
}

class HashHelper {

    public static String generateSHA256Hash(String secretKey, String stringToHash) {
        try {
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
        catch (Exception e) {
            return "";
        }
    }
}

class QueueUrlParams {

    private String eventId;
    private String hashCode;
    private boolean extendableCookie;
    private String queueITToken;
    private String queueITTokenWithoutHash;
    private Integer cookieValidityMinutes;
    private long timeStamp;
    private String queueId;
    private String redirectType;

    public QueueUrlParams() {
        this.eventId = "";
        this.hashCode = "";
        this.extendableCookie = false;
        this.queueITToken = "";
        this.queueITTokenWithoutHash = "";
        this.cookieValidityMinutes = null;
        this.timeStamp = 0;
        this.queueId = "";
    }

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

    public Integer getCookieValidityMinutes() {
        return this.cookieValidityMinutes;
    }

    public void setCookieValidityMinutes(Integer cookieValidityMinute) {
        this.cookieValidityMinutes = cookieValidityMinute;
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

    public void setRedirectType(String redirectType) {
        this.redirectType = redirectType;
    }

    public String getRedirectType() {
        return this.redirectType;
    }
}
