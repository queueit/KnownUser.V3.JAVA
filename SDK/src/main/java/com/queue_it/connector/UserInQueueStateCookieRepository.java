package com.queue_it.connector;

import java.util.HashMap;

interface IUserInQueueStateRepository {

    void store(
        String eventId,
        String queueId,
        Integer fixedCookieValidityMinutes,
        String cookieDomain,
        Boolean isCookieHttpOnly,
        Boolean isCookieSecure,
        String redirectType,
        String secretKey) throws Exception;

    StateInfo getState(String eventId,
            int cookieValidityMinutes,
            String secretKey,
            boolean validateTime);

    void cancelQueueCookie(
            String eventId,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure);

    void reissueQueueCookie(
        String eventId,
        int cookieValidityMinutes,
        String cookieDomain,
        Boolean isCookieHttpOnly,
        Boolean isCookieSecure,
        String secretKey);
}

class UserInQueueStateCookieRepository implements IUserInQueueStateRepository {

    private static final String QUEUEIT_DATA_KEY = "QueueITAccepted-SDFrts345E-V3";
    private static final String QUEUE_ID_KEY = "QueueId";
    private static final String HASH_KEY = "Hash";
    private static final String ISSUETIME_KEY = "IssueTime";
    private static final String REDIRECT_TYPE_KEY = "RedirectType";
    private static final String EVENT_ID_KEY = "EventId";
    private static final String FIXED_COOKIE_VALIDITY_MINUTES_KEY = "FixedValidityMins";
    private final ICookieManager cookieManager;

    public static String getCookieKey(String eventId) {
        return QUEUEIT_DATA_KEY + "_" + eventId;
    }

    public UserInQueueStateCookieRepository(ICookieManager cookieManeger) {
        this.cookieManager = cookieManeger;
    }

    @Override
    public void store(
        String eventId,
        String queueId,
        Integer fixedCookieValidityMinutes,
        String cookieDomain,
        Boolean isCookieHttpOnly,
        Boolean isCookieSecure,
        String redirectType,
        String secretKey) throws Exception {

        String cookieKey = getCookieKey(eventId);

        String cookieValue = createCookieValue(eventId, queueId, fixedCookieValidityMinutes, redirectType, secretKey);

        this.cookieManager.setCookie(cookieKey, cookieValue, 24 * 60 * 60, cookieDomain, isCookieHttpOnly, isCookieSecure);
    }

    private String createCookieValue(String eventId, String queueId, Integer fixedCookieValidityMinutes, String redirectType, String secretKey) throws Exception {
        String issueTime = Long.toString(System.currentTimeMillis() / 1000L);
        String fixedValidityStr = fixedCookieValidityMinutes != null ? String.valueOf(fixedCookieValidityMinutes) : "";

        String hashValue = HashHelper.generateSHA256Hash(secretKey, eventId + queueId
                + fixedValidityStr
                + redirectType
                + issueTime);
        String cookieValue = EVENT_ID_KEY + "=" + eventId + "&" + QUEUE_ID_KEY + "=" + queueId + "&"
                + (fixedCookieValidityMinutes != null ? (FIXED_COOKIE_VALIDITY_MINUTES_KEY + "=" + fixedValidityStr + "&") : "")
                + REDIRECT_TYPE_KEY + "=" + redirectType
                + "&" + ISSUETIME_KEY + "=" + issueTime + "&" + HASH_KEY + "=" + hashValue;
        return cookieValue;
    }

    private Boolean isCookieValid(
            String secretKey,
            HashMap<String, String> cookieNameValueMap,
            String eventId,
            int cookieValidityMinutes,
            boolean validateTime) {
        try {
            if (!cookieNameValueMap.containsKey(EVENT_ID_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(QUEUE_ID_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(REDIRECT_TYPE_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(ISSUETIME_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(HASH_KEY)) {
                return false;
            }

            String fixedCookieValidityMinutes = "";
            if (cookieNameValueMap.containsKey(FIXED_COOKIE_VALIDITY_MINUTES_KEY)) {
                fixedCookieValidityMinutes = cookieNameValueMap.get(FIXED_COOKIE_VALIDITY_MINUTES_KEY);
            }

            String hashValue = HashHelper.generateSHA256Hash(secretKey,
                    cookieNameValueMap.get(EVENT_ID_KEY)
                    + cookieNameValueMap.get(QUEUE_ID_KEY)
                    + fixedCookieValidityMinutes
                    + cookieNameValueMap.get(REDIRECT_TYPE_KEY)
                    + cookieNameValueMap.get(ISSUETIME_KEY));

            if (!hashValue.equals(cookieNameValueMap.get(HASH_KEY))) {
                return false;
            }
            if (!eventId.toLowerCase().equals(cookieNameValueMap.get(EVENT_ID_KEY).toLowerCase())) {
                return false;
            }

            if (validateTime) {
                int validity = cookieValidityMinutes;
                if (!Utils.isNullOrWhiteSpace(fixedCookieValidityMinutes)) {
                    validity = Integer.valueOf(fixedCookieValidityMinutes);
                }

                long expirationTime = Long.valueOf(cookieNameValueMap.get(ISSUETIME_KEY)) + (validity * 60);
                if (expirationTime < (System.currentTimeMillis() / 1000L)) {
                    return false;
                }
            }
            return true;

        } catch (Exception ex) {
        }
        return true;
    }

    public static HashMap<String, String> getCookieNameValueMap(String cookieValue) {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] cookieNameValues = cookieValue.split("&");

        for (int i = 0; i < cookieNameValues.length; ++i) {
            String[] arr = cookieNameValues[i].split("=");
            if (arr.length == 2) {
                result.put(arr[0], arr[1]);
            }
        }
        return result;
    }

    @Override
    public StateInfo getState(String eventId, int cookieValidityMinutes, String secretKey, boolean validateTime) {
        try {
            String cookieKey = getCookieKey(eventId);
            String cookieValue = this.cookieManager.getCookie(cookieKey);
            if (cookieValue == null) {
                return new StateInfo(false, false, null, null, null);
            }
            HashMap<String, String> cookieNameValueMap = UserInQueueStateCookieRepository.getCookieNameValueMap(cookieValue);
            if (!isCookieValid(secretKey, cookieNameValueMap, eventId, cookieValidityMinutes, validateTime)) {
                return new StateInfo(true, false, null, null, null);
            }

            return new StateInfo(true, true, cookieNameValueMap.get(QUEUE_ID_KEY),
                    cookieNameValueMap.get(FIXED_COOKIE_VALIDITY_MINUTES_KEY),
                    cookieNameValueMap.get(REDIRECT_TYPE_KEY));
        } catch (NumberFormatException ex) {
        }
        return new StateInfo(true, false, null, null, null);
    }

    @Override
    public void cancelQueueCookie(
            String eventId,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure) {
        String cookieKey = getCookieKey(eventId);
        cookieManager.setCookie(cookieKey, null, 0, cookieDomain, isCookieHttpOnly, isCookieSecure);
    }

    @Override
    public void reissueQueueCookie(
        String eventId,
        int cookieValidityMinutes,
        String cookieDomain,
        Boolean isCookieHttpOnly, Boolean isCookieSecure, String secretKey) {
        try {
            String cookieKey = getCookieKey(eventId);
            String cookieValueOld = this.cookieManager.getCookie(cookieKey);
            if (cookieValueOld == null) {
                return;
            }
            HashMap<String, String> cookieNameValueMap = getCookieNameValueMap(cookieValueOld);
            if (!isCookieValid(secretKey, cookieNameValueMap, eventId, cookieValidityMinutes, true)) {
                return;
            }
            Integer fixedCookieValidityMinutes = null;
            if (cookieNameValueMap.containsKey(FIXED_COOKIE_VALIDITY_MINUTES_KEY)) {
                fixedCookieValidityMinutes = Integer.valueOf(cookieNameValueMap.get(FIXED_COOKIE_VALIDITY_MINUTES_KEY));
            }
            String cookieValue = createCookieValue(eventId, cookieNameValueMap.get(QUEUE_ID_KEY),
                    fixedCookieValidityMinutes, cookieNameValueMap.get(REDIRECT_TYPE_KEY), secretKey);
            this.cookieManager.setCookie(cookieKey, cookieValue, 24 * 60 * 60, cookieDomain, isCookieHttpOnly, isCookieSecure);

        } catch (Exception ex) {
        }
    }
}

class StateInfo {

    private final boolean isFound;
    private final boolean isValid;
    private final String queueId;
    private final String redirectType;
    private final String fixedCookieValidityMinutes;

    public StateInfo(boolean isFound, boolean isValid, String queueid, String fixedCookieValidityMinutes,
            String redirectType) {
        this.isFound = isFound;
        this.isValid = isValid;
        this.queueId = queueid;
        this.fixedCookieValidityMinutes = fixedCookieValidityMinutes;
        this.redirectType = redirectType;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public boolean isFound() {
        return this.isFound;
    }
    public boolean isValid() {
        return this.isValid;
    }

    public String getRedirectType() {
        return this.redirectType;
    }

    public boolean isStateExtendable() {
        return this.isValid && Utils.isNullOrWhiteSpace(this.fixedCookieValidityMinutes);
    }
}
