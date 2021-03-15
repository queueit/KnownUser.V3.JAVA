package com.queue_it.connector.integrationconfig;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.security.Principal;
import java.util.*;


class HttpServletRequestMock implements HttpServletRequest {

    public Cookie[] CookiesValue;
    public String UserAgent = "";
    public String RequestURL;
    public String QueryString;
    public HashMap<String, String> Headers = new HashMap<>();

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cookie[] getCookies() {
        return this.CookiesValue;
    }

    @Override
    public long getDateHeader(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getHeader(String key) {
        if ("User-Agent".equals(key)) {
            return this.UserAgent;
        }

        if (this.Headers.containsKey(key)) {
            return this.Headers.get(key);
        }
        return "";
    }

    @Override
    public Enumeration<String> getHeaders(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIntHeader(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getMethod() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getQueryString() {
        return this.QueryString;
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isUserInRole(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRequestURI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(this.RequestURL);
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpSession getSession(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean authenticate(HttpServletResponse hsr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void login(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Part> getParts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Part getPart(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getAttribute(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCharacterEncoding(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServletInputStream getInputStream() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getParameter(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getParameterValues(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BufferedReader getReader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttribute(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAttribute(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRealPath(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

public class IntegrationEvaluatorTest {

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_And_NotMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.AND;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[0];

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertNull(result);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_And_Matched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.IsIgnoreCase = true;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.AND;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[1];
        httpContextMock.CookiesValue[0] = new Cookie("c1", "value1");

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_Or_NotMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.IsNegative = true;
        triggerPart2.IsIgnoreCase = true;
        triggerPart2.ValueToCompare = "tesT";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.OR;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[1];
        httpContextMock.CookiesValue[0] = new Cookie("c2", "value1");

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertNull(result);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_Or_Matched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "tesT";

        TriggerPart[] triggerParts = new TriggerPart[2];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.OR;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[1];
        httpContextMock.CookiesValue[0] = new Cookie("c1", "value1");

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_TwoTriggers_Matched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart[] triggerParts1 = new TriggerPart[1];
        triggerParts1[0] = triggerPart1;

        TriggerModel triggerModel1 = new TriggerModel();
        triggerModel1.LogicalOperator = LogicalOperatorType.AND;
        triggerModel1.TriggerParts = triggerParts1;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "*";

        TriggerPart[] triggerParts2 = new TriggerPart[1];
        triggerParts2[0] = triggerPart2;

        TriggerModel triggerModel2 = new TriggerModel();
        triggerModel2.LogicalOperator = LogicalOperatorType.AND;
        triggerModel2.TriggerParts = triggerParts2;

        TriggerModel[] triggerModels = new TriggerModel[2];
        triggerModels[0] = triggerModel1;
        triggerModels[1] = triggerModel2;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[0];

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_TwoTriggers_NotMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart[] triggerParts1 = new TriggerPart[1];
        triggerParts1[0] = triggerPart1;

        TriggerModel triggerModel1 = new TriggerModel();
        triggerModel1.LogicalOperator = LogicalOperatorType.AND;
        triggerModel1.TriggerParts = triggerParts1;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "tesT";

        TriggerPart[] triggerParts2 = new TriggerPart[1];
        triggerParts2[0] = triggerPart2;

        TriggerModel triggerModel2 = new TriggerModel();
        triggerModel2.LogicalOperator = LogicalOperatorType.AND;
        triggerModel2.TriggerParts = triggerParts2;

        TriggerModel[] triggerModels = new TriggerModel[2];
        triggerModels[0] = triggerModel1;
        triggerModels[1] = triggerModel2;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[0];

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertNull(result);
    }

    @Test
    public void GetMatchedIntegrationConfig_ThreeIntegrationsInOrder_SecondMatched() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart0 = new TriggerPart();
        triggerPart0.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart0.Operator = ComparisonOperatorType.EQUALS;
        triggerPart0.ValueToCompare = "value1";
        triggerPart0.CookieName = "c1";

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.ValueToCompare = "Value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart[] triggerParts0 = new TriggerPart[1];
        triggerParts0[0] = triggerPart0;

        TriggerPart[] triggerParts1 = new TriggerPart[1];
        triggerParts1[0] = triggerPart1;

        TriggerPart[] triggerParts2 = new TriggerPart[1];
        triggerParts2[0] = triggerPart2;

        TriggerModel triggerModel0 = new TriggerModel();
        triggerModel0.LogicalOperator = LogicalOperatorType.AND;
        triggerModel0.TriggerParts = triggerParts0;

        TriggerModel triggerModel1 = new TriggerModel();
        triggerModel1.LogicalOperator = LogicalOperatorType.AND;
        triggerModel1.TriggerParts = triggerParts1;

        TriggerModel triggerModel2 = new TriggerModel();
        triggerModel2.LogicalOperator = LogicalOperatorType.AND;
        triggerModel2.TriggerParts = triggerParts2;

        TriggerModel[] triggerModels0 = new TriggerModel[1];
        triggerModels0[0] = triggerModel0;

        TriggerModel[] triggerModels1 = new TriggerModel[1];
        triggerModels1[0] = triggerModel1;

        TriggerModel[] triggerModels2 = new TriggerModel[1];
        triggerModels2[0] = triggerModel2;

        IntegrationConfigModel integrationConfigModel0 = new IntegrationConfigModel();
        integrationConfigModel0.Name = "integration0";
        integrationConfigModel0.Triggers = triggerModels0;

        IntegrationConfigModel integrationConfigModel1 = new IntegrationConfigModel();
        integrationConfigModel1.Name = "integration1";
        integrationConfigModel1.Triggers = triggerModels1;

        IntegrationConfigModel integrationConfigModel2 = new IntegrationConfigModel();
        integrationConfigModel2.Name = "integration2";
        integrationConfigModel2.Triggers = triggerModels2;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[3];
        integrationConfigModels[0] = integrationConfigModel0;
        integrationConfigModels[1] = integrationConfigModel1;
        integrationConfigModels[2] = integrationConfigModel2;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[1];
        httpContextMock.CookiesValue[0] = new Cookie("c1", "Value1");

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertEquals("integration1", result.Name);
    }

    @Test
    public void GetMatchedIntegrationConfig_OneTrigger_And_NotMatched_UserAgent() throws Exception {
        IntegrationEvaluator testObject = new IntegrationEvaluator();

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.ValidatorType = ValidatorType.COOKIE_VALIDATOR;
        triggerPart1.Operator = ComparisonOperatorType.EQUALS;
        triggerPart1.IsIgnoreCase = true;
        triggerPart1.ValueToCompare = "value1";
        triggerPart1.CookieName = "c1";

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.ValidatorType = ValidatorType.URL_VALIDATOR;
        triggerPart2.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart2.UrlPart = UrlPartType.PAGE_URL;
        triggerPart2.ValueToCompare = "test";

        TriggerPart triggerPart3 = new TriggerPart();
        triggerPart3.ValidatorType = ValidatorType.USERAGENT_VALIDATOR;
        triggerPart3.Operator = ComparisonOperatorType.CONTAINS;
        triggerPart3.ValueToCompare = "googlebot";
        triggerPart3.IsNegative = true;
        triggerPart3.IsIgnoreCase = true;

        TriggerPart[] triggerParts = new TriggerPart[3];
        triggerParts[0] = triggerPart1;
        triggerParts[1] = triggerPart2;
        triggerParts[2] = triggerPart3;

        TriggerModel triggerModel = new TriggerModel();
        triggerModel.LogicalOperator = LogicalOperatorType.AND;
        triggerModel.TriggerParts = triggerParts;

        TriggerModel[] triggerModels = new TriggerModel[1];
        triggerModels[0] = triggerModel;

        IntegrationConfigModel integrationConfigModel = new IntegrationConfigModel();
        integrationConfigModel.Name = "integration1";
        integrationConfigModel.Triggers = triggerModels;

        IntegrationConfigModel[] integrationConfigModels = new IntegrationConfigModel[1];
        integrationConfigModels[0] = integrationConfigModel;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = integrationConfigModels;

        String url = "http://test.tesdomain.com:8080/test?q=2";

        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.CookiesValue = new Cookie[1];
        httpContextMock.CookiesValue[0] = new Cookie("c1", "value1");
        httpContextMock.UserAgent = "Googlebot";

        IntegrationConfigModel result = testObject.getMatchedIntegrationConfig(customerIntegration, url, httpContextMock);
        assertNull(result);
    }
}
