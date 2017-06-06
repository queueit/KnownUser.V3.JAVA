package queueit.knownuserv3.sdk;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import queueit.knownuserv3.sdk.integrationconfig.CustomerIntegration;

@WebServlet(name = "ProcessServlet", urlPatterns = {"/ProcessServlet"})
public class ProcessServlet extends HttpServlet {

    boolean isQueueItEnabled = true; // move this flag to your config file or database for easy enabling / disabling of Queue-it integration

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // All page requests are validated against the KnownUser library to ensure only users that have been through the queue are allowed in
        // This example is using the IntegrationConfigProvider example to download and cache the integration configuration from Queue-it
        // The downloaded integration configuration will make sure that only configured pages are protected
        if (isQueueItEnabled) {
            doValidation(request, response);  //Default Queue-it integration
        }
        
        // This shows an alternative implementation
        // This example is manually specifiying the configuartion to use, using the EventConfig() class
        // Here you also manually need to ensure that only the relevant page requests are validated
        // Also ensure that only page requests (and not e.g. image requests) are validated
        //if (isQueueItEnabled) {
          // doValidationByLocalEventConfig(request, response); //Example of alternative implementation using local event configuration
        //}
    }
    
    private void doValidation(HttpServletRequest request, HttpServletResponse response) {
        try {
            String customerId = "Your Queue-it customer ID";
            String secretKey = "Your 72 char secrete key as specified in Go Queue-it self-service platform";

            String queueitToken = request.getParameter(KnownUser.QueueITTokenKey);
            String pureUrl = getPureUrl(request);
            
            CustomerIntegration integrationConfig = IntegrationConfigProvider.getCachedIntegrationConfig(customerId);

            //Verify if the user has been through the queue
            RequestValidationResult validationResult = KnownUser.validateRequestByIntegrationConfig(
                    pureUrl, queueitToken, integrationConfig, customerId, request, response, secretKey);

            if (validationResult.doRedirect()) {
                //Send the user to the queue - either becuase hash was missing or becuase is was invalid
                response.sendRedirect(validationResult.getRedirectUrl());
            } else {
                String queryString = request.getQueryString();
                //Request can continue - we remove queueittoken form querystring parameter to avoid sharing of user specific token
                if (queryString != null && queryString.contains(KnownUser.QueueITTokenKey)) {
                    response.sendRedirect(pureUrl);
                }
            }
        } catch (Exception ex) {
            //There was an error validationg the request
            //Use your own logging framework to log the Exception
            //This was a configuration exception, so we let the user continue            
        }
    }
    
    private void doValidationByLocalEventConfig(HttpServletRequest request, HttpServletResponse response) {
        try {
            String customerId = "Your Queue-it customer ID";
            String secretKey = "Your 72 char secrete key as specified in Go Queue-it self-service platform";

            String queueitToken = request.getParameter(KnownUser.QueueITTokenKey);
            String pureUrl = getPureUrl(request);
            
            EventConfig eventConfig = new EventConfig();
            eventConfig.setEventId("event1"); //ID of the queue to use           
            eventConfig.setCookieDomain(".mydomain.com"); //Optional - Domain name where the Queue-it session cookie should be saved. Default is to save on the domain of the request
            eventConfig.setQueueDomain("queue.mydomain.com"); //Optional - Domian name of the queue. Default is [CustomerId].queue-it.net
            eventConfig.setCookieValidityMinute(15); //Optional - Validity of the Queue-it session cookie. Default is 10 minutes
            eventConfig.setExtendCookieValidity(false); //Optional - Should the Queue-it session cookie validity time be extended each time the validation runs? Default is true.
            eventConfig.setCulture("en-US"); //Optional - Culture of the queue ticket layout in the format specified here: https://msdn.microsoft.com/en-us/library/ee825488(v=cs.20).aspx Default is to use what is specified on Event
            eventConfig.setLayoutName("MyCustomLayoutName"); //Optional - Name of the queue ticket layout - e.g. "Default layout by Queue-it". Default is to use what is specified on the Event
            
            //Verify if the user has been through the queue
            RequestValidationResult validationResult = KnownUser.validateRequestByLocalEventConfig(pureUrl, queueitToken, eventConfig, customerId, request, response, secretKey);

            if (validationResult.doRedirect()) {
                //Send the user to the queue - either becuase hash was missing or becuase is was invalid
                response.sendRedirect(validationResult.getRedirectUrl());
            } else {
                String queryString = request.getQueryString();
                //Request can continue - we remove queueittoken form querystring parameter to avoid sharing of user specific token
                if (queryString != null && queryString.contains(KnownUser.QueueITTokenKey)) {
                    response.sendRedirect(pureUrl);
                }
            }
        } catch (Exception ex) {
            //There was an error validationg the request
            //Please log the error and let user continue            
        }
    }
    
    private String getPureUrl(HttpServletRequest request){
        Pattern pattern = Pattern.compile("([\\?&])(" + KnownUser.QueueITTokenKey + "=[^&]*)", Pattern.CASE_INSENSITIVE);
        String queryString = request.getQueryString();
        String url = request.getRequestURL().toString() + (queryString != null ? ("?" + queryString) : "");

        String pureUrl = pattern.matcher(url).replaceAll("");
        return pureUrl;
    }
}
