package queueit.knownuserv3.sdk;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

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
            int cookieValidityMinute,
            String cookieDomain,
            String secretKey
    );
}

class UserInQueueService implements IUserInQueueService {

    public static final String SDK_VERSION = "3.3.2";
    private final IUserInQueueStateRepository _userInQueueStateRepository;

    public UserInQueueService(
            IUserInQueueStateRepository queueStateRepository) {
        this._userInQueueStateRepository = queueStateRepository;
    }

    @Override
    public RequestValidationResult validateQueueRequest(
            String targetUrl,
            String queueitToken,
            QueueEventConfig config,
            String customerId,
            String secretKey
    ) throws Exception {
        StateInfo stateInfo = this._userInQueueStateRepository.getState(config.getEventId(), secretKey);
        if (stateInfo.isValid()) {
            if (stateInfo.isStateExtendable() && config.getExtendCookieValidity()) {
                this._userInQueueStateRepository.store(config.getEventId(),
                        stateInfo.getQueueId(),
                        true,
                        config.getCookieDomain(),
                        config.getCookieValidityMinute(),
                        secretKey);
            }
            return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), stateInfo.getQueueId(), null);
        }

        QueueUrlParams queueParmas = QueueParameterHelper.extractQueueParams(queueitToken);

        if (queueParmas != null) {
            return getQueueITTokenValidationResult(targetUrl, config.getEventId(), config, queueParmas, customerId, secretKey);
        } else {
            return getInQueueRedirectResult(targetUrl, config, customerId);
        }
    }

    private RequestValidationResult getQueueITTokenValidationResult(
            String targetUrl,
            String eventId,
            QueueEventConfig config,
            QueueUrlParams queueParams,
            String customerId,
            String secretKey) throws Exception {
        String calculatedHash = HashHelper.generateSHA256Hash(secretKey, queueParams.getQueueITTokenWithoutHash());
        if (!Objects.equals(calculatedHash.toUpperCase(), queueParams.getHashCode().toUpperCase())) {
            return getVaidationErrorResult(customerId, targetUrl, config, queueParams, "hash");
        }

        if (!Objects.equals(queueParams.getEventId().toUpperCase(), eventId.toUpperCase())) {
            return getVaidationErrorResult(customerId, targetUrl, config, queueParams, "eventid");
        }

        if (queueParams.getTimeStamp() < System.currentTimeMillis() / 1000L) {
            return getVaidationErrorResult(customerId, targetUrl, config, queueParams, "timestamp");
        }

        this._userInQueueStateRepository.store(
                config.getEventId(),
                queueParams.getQueueId(),
                queueParams.getExtendableCookie(),
                config.getCookieDomain(),
                queueParams.getCookieValidityMinute() != null ? queueParams.getCookieValidityMinute() : config.getCookieValidityMinute(),
                secretKey);

        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), queueParams.getQueueId(), null);
    }

    private RequestValidationResult getVaidationErrorResult(
            String customerId,
            String targetUrl,
            QueueEventConfig config,
            QueueUrlParams qParams,
            String errorCode) throws Exception {

        String query = getQueryString(customerId, config.getEventId(), config.getVersion(), config.getCulture(), config.getLayoutName())
                + "&queueittoken=" + qParams.getQueueITToken()
                + "&ts=" + System.currentTimeMillis() / 1000L;
        if(!Utils.isNullOrWhiteSpace(targetUrl))
        {
            query += "&t=" + URLEncoder.encode(targetUrl, "UTF-8");
        }
        String domainAlias = config.getQueueDomain();
        if (!domainAlias.endsWith("/")) {
            domainAlias = domainAlias + "/";
        }
        String redirectUrl = "https://" + domainAlias + "error/" + errorCode + "/?" + query;
        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), null, redirectUrl);
    }

    private RequestValidationResult getInQueueRedirectResult(
            String targetUrl,
            QueueEventConfig config,
            String customerId) throws Exception {

        String redirectUrl = "https://" + config.getQueueDomain() + "?"
                + getQueryString(customerId, config.getEventId(), config.getVersion(), config.getCulture(), config.getLayoutName());
        if(!Utils.isNullOrWhiteSpace(targetUrl)) {
            redirectUrl += "&t=" + URLEncoder.encode(targetUrl, "UTF-8");
        }
                
        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), null, redirectUrl);
    }

    private String getQueryString(
            String customerId,
            String eventId,
            int configVersion,
            String culture,
            String layoutName) throws Exception {
        ArrayList<String> queryStringList = new ArrayList<>();
        queryStringList.add("c=" + URLEncoder.encode(customerId, "UTF-8"));
        queryStringList.add("e=" + URLEncoder.encode(eventId, "UTF-8"));
        queryStringList.add("ver=v3-java-" + URLEncoder.encode(SDK_VERSION, "UTF-8"));
        queryStringList.add("cver=" + URLEncoder.encode(String.valueOf(configVersion), "UTF-8"));

        if (!Utils.isNullOrWhiteSpace(culture)) {
            queryStringList.add("cid=" + URLEncoder.encode(culture, "UTF-8"));
        }

        if (!Utils.isNullOrWhiteSpace(layoutName)) {
            queryStringList.add("l=" + URLEncoder.encode(layoutName, "UTF-8"));
        }

        return String.join("&", queryStringList);
    }
   
    @Override
    public void extendQueueCookie(
            String eventId,
            int cookieValidityMinute,
            String cookieDomain,
            String secretKey) {
        this._userInQueueStateRepository.extendQueueCookie(eventId, cookieValidityMinute, cookieDomain, secretKey);
    }
    
    @Override
    public RequestValidationResult validateCancelRequest(
            String targetUrl, CancelEventConfig config, String customerId, String secretKey) throws Exception {
            
        StateInfo state = _userInQueueStateRepository.getState(config.getEventId(), secretKey);

        if (state.isValid()) {
            this._userInQueueStateRepository.cancelQueueCookie(config.getEventId(), config.getCookieDomain());

            String query = getQueryString(customerId, config.getEventId(), config.getVersion(), null, null);
            
            if(targetUrl != null)
                query += "&r=" + URLEncoder.encode(targetUrl, "UTF-8");

            String domainAlias = config.getQueueDomain();
            if (!domainAlias.endsWith("/"))
                domainAlias = domainAlias + "/";

            String redirectUrl = "https://" + domainAlias + "cancel/" + customerId + "/" + config.getEventId() + "/?" + query;

            return new RequestValidationResult(ActionType.CANCEL_ACTION, config.getEventId(), state.getQueueId(), redirectUrl);
        }
        else
        {
            return new RequestValidationResult(ActionType.CANCEL_ACTION, config.getEventId(), null, null);
        }
    }
}
