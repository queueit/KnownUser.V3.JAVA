/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queueit.knownuserv3.sdk;

import java.util.HashMap;
import java.util.Objects;

interface IUserInQueueStateRepository {

    void store(
            String eventId,
            String queueId,
            boolean isStateExtendable,
            String cookieDomain,
            int cookieValidityMinute,
            String customerSecretKey) throws Exception;

    StateInfo getState(String eventId,
            String customerSecretKey);

    void cancelQueueCookie(
            String eventId,
            String cookieDomain);

    void extendQueueCookie(
            String eventId,
            int cookieValidityMinute,
            String cookieDomain,
            String secretKey
    );
}

class UserInQueueStateCookieRepository implements IUserInQueueStateRepository {

    private static final String QUEUEIT_DATA_KEY = "QueueITAccepted-SDFrts345E-V3";
    private static final String QUEUE_ID_KEY = "QueueId";
    private static final String IS_COOKIE_EXTENDABLE_KEY = "IsCookieExtendable";
    private static final String HASH_KEY = "Hash";
    private static final String EXPIRES_KEY = "Expires";

    private final ICookieManager cookieManeger;

    public static String getCookieKey(String eventId) {
        return QUEUEIT_DATA_KEY + "_" + eventId;
    }

    public UserInQueueStateCookieRepository(ICookieManager cookieManeger) {
        this.cookieManeger = cookieManeger;
    }

    @Override
    public void store(
            String eventId,
            String queueId,
            boolean isStateExtendable,
            String cookieDomain,
            int cookieValidityMinute,
            String secretKey) throws Exception {

        String cookieKey = getCookieKey(eventId);

        Long expirationTime = System.currentTimeMillis() / 1000L + cookieValidityMinute * 60;
        String isStateExtendableString = Boolean.toString(isStateExtendable);

        String cookieValue = createCookieValue(queueId, isStateExtendableString, String.valueOf(expirationTime), secretKey);

        this.cookieManeger.setCookie(cookieKey, cookieValue, 24 * 60 * 60, cookieDomain);
    }



    private String createCookieValue(String queueId, String isStateExtendable, String expirationTime, String secretKey) throws Exception {
        String hashValue = HashHelper.generateSHA256Hash(secretKey, queueId
                + isStateExtendable
                + expirationTime);
        String cookieValue = QUEUE_ID_KEY + "=" + queueId + "&" + IS_COOKIE_EXTENDABLE_KEY
                + "=" + isStateExtendable + "&" + EXPIRES_KEY + "=" + expirationTime + "&" + HASH_KEY + "=" + hashValue;
        return cookieValue;
    }

    private Boolean isCookieValid(
            HashMap<String, String> cookieNameValueMap,
            String secretKey
    ) {
        try {
            if (!cookieNameValueMap.containsKey(IS_COOKIE_EXTENDABLE_KEY)
                    || Utils.isNullOrWhiteSpace(IS_COOKIE_EXTENDABLE_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(QUEUE_ID_KEY)
                    || Utils.isNullOrWhiteSpace(QUEUE_ID_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(EXPIRES_KEY)
                    || Utils.isNullOrWhiteSpace(EXPIRES_KEY)) {
                return false;
            }
            if (!cookieNameValueMap.containsKey(HASH_KEY)
                    || Utils.isNullOrWhiteSpace(HASH_KEY)) {
                return false;
            }

            String hashValue = HashHelper.generateSHA256Hash(secretKey,
                    cookieNameValueMap.get(QUEUE_ID_KEY)
                    + cookieNameValueMap.get(IS_COOKIE_EXTENDABLE_KEY)
                    + cookieNameValueMap.get(EXPIRES_KEY));

            if (!Objects.equals(hashValue, cookieNameValueMap.get(HASH_KEY))) {
                return false;
            }
            Long nowSecond = System.currentTimeMillis() / 1000L;
            if (Long.parseLong(cookieNameValueMap.get(EXPIRES_KEY)) < nowSecond) {
                return false;
            }
        } catch (Exception ex) {
        }
        return true;

    }

    public static HashMap<String, String> getCookieNameValueMap(String cookieValue) {
        HashMap<String, String> result = new HashMap<>();
        String[] cookieNameValues = cookieValue.split("&");

        if (cookieNameValues.length < 4) {
            return result;
        }
        for (int i = 0; i < 4; ++i) {
            String[] arr = cookieNameValues[i].split("=");
            if (arr.length == 2) {
                result.put(arr[0], arr[1]);
            }
        }
        return result;
    }

    @Override
    public StateInfo getState(String eventId, String secretKey) {
        try {
            String cookieKey = getCookieKey(eventId);
            String cookieValue = this.cookieManeger.getCookie(cookieKey);
            if (cookieValue == null) {
                return new StateInfo(false, null, false,0);
            }
            HashMap<String, String> cookieNameValueMap = UserInQueueStateCookieRepository.getCookieNameValueMap(cookieValue);
            if (!isCookieValid(cookieNameValueMap, secretKey)) {
                return new StateInfo(false, null, false,0);
            }

            return new StateInfo(true, cookieNameValueMap.get(QUEUE_ID_KEY),
                    Boolean.parseBoolean(cookieNameValueMap.get(IS_COOKIE_EXTENDABLE_KEY)),
            Long.parseLong(cookieNameValueMap.get(EXPIRES_KEY)));
        } catch (Exception ex) {

        }
        return new StateInfo(false, null, false,0);
    }

    @Override
    public void cancelQueueCookie(
            String eventId,
            String cookieDomain) {
        String cookieKey = getCookieKey(eventId);
        if (cookieManeger.getCookie(cookieKey) != null) {
            cookieManeger.setCookie(cookieKey, null, 0, cookieDomain);
        }
    }
    @Override
        public void extendQueueCookie(
            String eventId,
            int cookieValidityMinute,
            String cookieDomain,
            String secretKey) {
        try {
            String cookieKey = getCookieKey(eventId);
            String cookieValueOld = this.cookieManeger.getCookie(cookieKey);
            if (cookieValueOld == null) {
                return;
            }
            HashMap<String, String> cookieNameValueMap = getCookieNameValueMap(cookieValueOld);
            if (!isCookieValid(cookieNameValueMap, secretKey)) {
                return;
            }
            long expirationTime = System.currentTimeMillis() / 1000L + cookieValidityMinute * 60;
            String cookieValue = createCookieValue(cookieNameValueMap.get(QUEUE_ID_KEY), cookieNameValueMap.get(IS_COOKIE_EXTENDABLE_KEY),
                    String.valueOf(expirationTime), secretKey);
            this.cookieManeger.setCookie(cookieKey, cookieValue, 24 * 60 * 60, cookieDomain);
        } catch (Exception ex) {

        }
    }

}

interface ICookieManager {

    void setCookie(String cookieName, String cookieValue, int maxAg, String cookieDomain);

    String getCookie(String cookieName);
}

class StateInfo {

    private final boolean isValid;
    private final String queueId;
    private final boolean isStateExtendable;
    private final long expires;


    public StateInfo(boolean isValid, String queueid, boolean isStateExtendable, long expires /*used in tests*/) {
        this.isValid = isValid;
        this.queueId = queueid;
        this.isStateExtendable = isStateExtendable;
        this.expires = expires;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public boolean isStateExtendable() {
        return this.isStateExtendable;
    }
    
    //used in tests
    public long getExpires() {
        return this.expires;
    }
}
