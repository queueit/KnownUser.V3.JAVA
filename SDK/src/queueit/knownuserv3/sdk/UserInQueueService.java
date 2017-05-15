package queueit.knownuserv3.sdk;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

interface IUserInQueueService {

    RequestValidationResult validateRequest(
            String targetUrl,
            String queueitToken,
            EventConfig config,
            String customerId,
            String secretKey) throws Exception;

    void cancelQueueCookie(String eventId, String cookieDomain);

    void extendQueueCookie(
            String eventId,
            int cookieValidityMinute,
             String cookieDomain,
            String secretKey
    );

}

class UserInQueueService implements IUserInQueueService {

    private static final String SDK_VERSION = "1.0.0.0";
    private final IUserInQueueStateRepository _userInQueueStateRepository;

    public UserInQueueService(
            IUserInQueueStateRepository queueStateRepository) {
        this._userInQueueStateRepository = queueStateRepository;
    }

    @Override
    public RequestValidationResult validateRequest(
            String targetUrl,
            String queueitToken,
            EventConfig config,
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
            return new RequestValidationResult(config.getEventId(), stateInfo.getQueueId(), null);
        }

        QueueUrlParams queueParmas = QueueParameterHelper.extractQueueParams(queueitToken);

        if (queueParmas != null) {
            return GetQueueITTokenValidationResult(targetUrl, config.getEventId(), config, queueParmas, customerId, secretKey);
        } else {
            return GetInQueueRedirectResult(targetUrl, config, customerId);
        }
    }

    private RequestValidationResult GetQueueITTokenValidationResult(
            String targetUrl,
            String eventId,
            EventConfig config,
            QueueUrlParams queueParams,
            String customerId,
            String secretKey) throws Exception {
        String calculatedHash = HashHelper.generateSHA256Hash(secretKey, queueParams.getQueueITTokenWithoutHash());
        if (!Objects.equals(calculatedHash.toUpperCase(),queueParams.getHashCode().toUpperCase())) {
            return GetVaidationErrorResult(customerId, targetUrl, config, queueParams, "hash");
        }

        if (!Objects.equals(queueParams.getEventId().toUpperCase(),eventId.toUpperCase())) {
            return GetVaidationErrorResult(customerId, targetUrl, config, queueParams, "eventid");
        }

        if (queueParams.getTimeStamp() < System.currentTimeMillis() / 1000L) {
            return GetVaidationErrorResult(customerId, targetUrl, config, queueParams, "timestamp");
        }

        this._userInQueueStateRepository.store(
                config.getEventId(),
                queueParams.getQueueId(),
                queueParams.getExtendableCookie(),
                config.getCookieDomain(),
                queueParams.getCookieValidityMinute() != null ? queueParams.getCookieValidityMinute() : config.getCookieValidityMinute(),
                secretKey);

        return new RequestValidationResult(config.getEventId(), queueParams.getQueueId(), null);
    }

    private RequestValidationResult GetVaidationErrorResult(
            String customerId,
            String targetUrl,
            EventConfig config,
            QueueUrlParams qParams,
            String errorCode) throws Exception {

        String query = GetQueryString(customerId, config)
                + "&queueittoken=" + qParams.getQueueITToken()
                + "&ts=" + System.currentTimeMillis() / 1000L
                + "&t=" + URLEncoder.encode(targetUrl, "UTF-8");
        String domainAlias = config.getQueueDomain();
        if (!domainAlias.endsWith("/")) {
            domainAlias = domainAlias + "/";
        }
        String redirectUrl = "https://" + domainAlias + "error/" + errorCode + "?" + query;
        return new RequestValidationResult(config.getEventId(), null, redirectUrl);

    }

    private RequestValidationResult GetInQueueRedirectResult(
            String targetUrl,
            EventConfig config,
            String customerId) throws Exception {

        String redirectUrl = "https://" + config.getQueueDomain() + "?"
                + GetQueryString(customerId, config)
                + "&t=" + URLEncoder.encode(targetUrl, "UTF-8");
        return new RequestValidationResult(config.getEventId(), null, redirectUrl);
    }

    private String GetQueryString(
            String customerId,
            EventConfig config) throws Exception {
        ArrayList<String> queryStringList = new ArrayList<>();
        queryStringList.add("c=" + URLEncoder.encode(customerId, "UTF-8"));
        queryStringList.add("e=" + URLEncoder.encode(config.getEventId(), "UTF-8"));
        queryStringList.add("ver=v3-" + URLEncoder.encode(SDK_VERSION, "UTF-8"));
        queryStringList.add("cver=" + URLEncoder.encode(String.valueOf(config.getVersion()), "UTF-8"));

        if (!Utils.isNullOrWhiteSpace(config.getCulture())) {
            queryStringList.add("cid=" + URLEncoder.encode(config.getCulture(), "UTF-8"));
        }

        if (!Utils.isNullOrWhiteSpace(config.getLayoutName())) {
            queryStringList.add("l=" + URLEncoder.encode(config.getLayoutName(), "UTF-8"));
        }

        return String.join("&", queryStringList);
    }
    
    @Override
    public void cancelQueueCookie(String eventId,  String cookieDomain)  {
        this._userInQueueStateRepository.cancelQueueCookie(eventId, cookieDomain);
    }

    @Override
    public void extendQueueCookie(
            String eventId,
            int cookieValidityMinute,
             String cookieDomain,
            String secretKey) {
        this._userInQueueStateRepository.extendQueueCookie(eventId, cookieValidityMinute,cookieDomain, secretKey);
    }
}
