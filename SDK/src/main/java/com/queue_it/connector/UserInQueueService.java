package com.queue_it.connector;

import java.util.ArrayList;

interface IUserInQueueService {

    RequestValidationResult validateQueueRequest(
        String targetUrl,
        String queueitToken,
        QueueEventConfig config,
        String customerId,
        String secretKey) throws Exception;

    RequestValidationResult validateCancelRequest(
        String targetUrl,
        CancelEventConfig config,
        String customerId,
        String secretKey) throws Exception;

    void extendQueueCookie(
        String eventId,
        int cookieValidityMinutes,
        String cookieDomain,
        Boolean isCookieHttpOnly,
        Boolean isCookieSecure,
        String secretKey);

    RequestValidationResult getIgnoreActionResult(String actionName);
}

class UserInQueueService implements IUserInQueueService {

    public static final String SDK_VERSION = "v3-java-" + "3.7.0";
    public final IUserInQueueStateRepository _userInQueueStateRepository;

    public UserInQueueService(IUserInQueueStateRepository queueStateRepository) {
        this._userInQueueStateRepository = queueStateRepository;
    }

    @Override
    public RequestValidationResult validateQueueRequest(
        String targetUrl,
        String queueitToken,
        QueueEventConfig config,
        String customerId,
        String secretKey) throws Exception {
        StateInfo stateInfo = this._userInQueueStateRepository.getState(config.getEventId(), config.getCookieValidityMinute(), secretKey, true);

        if (stateInfo.isValid()) {
            if (stateInfo.isStateExtendable() && config.getExtendCookieValidity()) {
                this._userInQueueStateRepository.store(
                    config.getEventId(),
                    stateInfo.getQueueId(),
                    null,
                    config.getCookieDomain(),
                    config.getIsCookieHttpOnly(),
                    config.getIsCookieSecure(),
                    stateInfo.getRedirectType(),
                    secretKey
                );
            }
            return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), stateInfo.getQueueId(), null, stateInfo.getRedirectType(), config.getActionName());
        }
        QueueUrlParams queueParams = QueueParameterHelper.extractQueueParams(queueitToken);
        RequestValidationResult requestValidationResult;
        boolean isTokenValid = false;

        if (queueParams != null) {
            TokenValidationResult tokenValidationResult = validateToken(config, queueParams, secretKey);
            isTokenValid = tokenValidationResult.isValid();

            if (isTokenValid) {
                requestValidationResult = getValidTokenResult(config, queueParams, secretKey);
            } else {
                requestValidationResult = getErrorResult(customerId, targetUrl, config, queueParams, tokenValidationResult.getErrorCode());
            }
        } else {
            requestValidationResult = getQueueResult(targetUrl, config, customerId);
        }

        if (stateInfo.isFound() && !isTokenValid) {
            this._userInQueueStateRepository.cancelQueueCookie(config.getEventId(), config.getCookieDomain(),config.getIsCookieHttpOnly(), config.getIsCookieSecure());
        }

        return requestValidationResult;
    }

    private RequestValidationResult getValidTokenResult(
        QueueEventConfig config,
        QueueUrlParams queueParams,
        String secretKey) throws Exception {

        this._userInQueueStateRepository.store(
            config.getEventId(),
            queueParams.getQueueId(),
            queueParams.getCookieValidityMinutes(),
            config.getCookieDomain(),
            config.getIsCookieHttpOnly(), config.getIsCookieSecure(), queueParams.getRedirectType(),
            secretKey
        );

        return new RequestValidationResult(
            ActionType.QUEUE_ACTION,
            config.getEventId(),
            queueParams.getQueueId(),
            null,
            queueParams.getRedirectType(),
            config.getActionName());
    }

    private RequestValidationResult getErrorResult(
        String customerId,
        String targetUrl,
        QueueEventConfig config,
        QueueUrlParams qParams,
        String errorCode) throws Exception {

        String query = getQueryString(customerId, config.getEventId(), config.getVersion(), config.getActionName(), config.getCulture(), config.getLayoutName())
            + "&queueittoken=" + qParams.getQueueITToken() + "&ts=" + System.currentTimeMillis() / 1000L;

        if (!Utils.isNullOrWhiteSpace(targetUrl)) {
            query += "&t=" + Utils.encodeUrl(targetUrl);
        }

        String redirectUrl = generateRedirectUrl(config.getQueueDomain(), "error/" + errorCode + "/", query);

        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), null, redirectUrl, null, config.getActionName());
    }

    private RequestValidationResult getQueueResult(
        String targetUrl,
        QueueEventConfig config,
        String customerId) throws Exception {

        String query = getQueryString(customerId, config.getEventId(), config.getVersion(), config.getActionName(), config.getCulture(), config.getLayoutName());

        if (!Utils.isNullOrWhiteSpace(targetUrl)) {
            query += "&t=" + Utils.encodeUrl(targetUrl);

        }
        String redirectUrl = generateRedirectUrl(config.getQueueDomain(), "", query);

        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), null, redirectUrl, null, config.getActionName());
    }

    private String getQueryString(String customerId, String eventId, int configVersion, String actionName, String culture, String layoutName) throws Exception {

        ArrayList<String> queryStringList = new ArrayList<String>();
        queryStringList.add("c=" + Utils.encodeUrl(customerId));
        queryStringList.add("e=" + Utils.encodeUrl(eventId));
        queryStringList.add("ver=" + Utils.encodeUrl(SDK_VERSION));
        queryStringList.add("cver=" + Utils.encodeUrl(String.valueOf(configVersion)));
        queryStringList.add("man=" + Utils.encodeUrl(actionName));

        if (!Utils.isNullOrWhiteSpace(culture)) {
            queryStringList.add("cid=" + Utils.encodeUrl(culture));
        }

        if (!Utils.isNullOrWhiteSpace(layoutName)) {
            queryStringList.add("l=" + Utils.encodeUrl(layoutName));
        }

        return Utils.join("&", queryStringList);
    }

    private String generateRedirectUrl(String queueDomain, String uriPath, String query) throws Exception {
        if (!queueDomain.endsWith("/")) {
            queueDomain = queueDomain + "/";
        }
        return "https://" + queueDomain + uriPath + "?" + query;
    }

    private TokenValidationResult validateToken(QueueEventConfig config, QueueUrlParams queueParams, String secretKey) throws Exception {

        String calculatedHash = HashHelper.generateSHA256Hash(secretKey, queueParams.getQueueITTokenWithoutHash());
        if (!calculatedHash.toUpperCase().equals(queueParams.getHashCode().toUpperCase())) {
            return new TokenValidationResult(false, "hash");
        }

        if (!config.getEventId().toUpperCase().equals(queueParams.getEventId().toUpperCase())) {
            return new TokenValidationResult(false, "eventid");
        }

        if (queueParams.getTimeStamp() < System.currentTimeMillis() / 1000L) {
            return new TokenValidationResult(false, "timestamp");
        }

        return new TokenValidationResult(true, null);
    }

    @Override
    public void extendQueueCookie(
        String eventId,
        int cookieValidityMinute,
        String cookieDomain,
        Boolean isCookieHttpOnly,
        Boolean isCookieSecure,
        String secretKey) {
        this._userInQueueStateRepository.reissueQueueCookie(eventId, cookieValidityMinute, cookieDomain, isCookieHttpOnly, isCookieSecure, secretKey);
    }

    @Override
    public RequestValidationResult validateCancelRequest(
        String targetUrl,
        CancelEventConfig config,
        String customerId,
        String secretKey) throws Exception {

        StateInfo state = _userInQueueStateRepository.getState(config.getEventId(), -1, secretKey, false);

        if (state.isValid()) {
            this._userInQueueStateRepository.cancelQueueCookie(config.getEventId(), config.getCookieDomain(), config.getIsCookieHttpOnly(), config.getIsCookieSecure());
            String uriPath = "cancel/" + customerId + "/" + config.getEventId();

            String queueId = state.getQueueId();
            if(queueId != null && !queueId.trim().isEmpty()) {
                uriPath += "/" + queueId;
            }

            String query = getQueryString(customerId, config.getEventId(), config.getVersion(), config.getActionName(), null, null);

            if (!Utils.isNullOrWhiteSpace(targetUrl)) {
                query += "&r=" + Utils.encodeUrl(targetUrl);
            }
            String redirectUrl = generateRedirectUrl(config.getQueueDomain(), uriPath, query);

            return new RequestValidationResult(ActionType.CANCEL_ACTION, config.getEventId(), state.getQueueId(), redirectUrl, state.getRedirectType(), config.getActionName());
        } else {
            return new RequestValidationResult(ActionType.CANCEL_ACTION, config.getEventId(), null, null, null, config.getActionName());
        }
    }

    @Override
    public RequestValidationResult getIgnoreActionResult(String actionName) {
        return new RequestValidationResult(ActionType.IGNORE_ACTION, null, null, null, null, actionName);
    }

    private class TokenValidationResult {
        private final boolean isValid;
        private final String errorCode;

        public TokenValidationResult(boolean isValid, String errorCode) {
            this.isValid = isValid;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return this.isValid;
        }

        public String getErrorCode() {
            return this.errorCode;
        }
    }
}