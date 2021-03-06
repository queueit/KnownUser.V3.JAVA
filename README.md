# Queue-it KnownUser SDK for Java 
Before getting started please read the [documentation](https://github.com/queueit/Documentation/tree/main/serverside-connectors) to get acquainted with server-side connectors.

This connector supports Java 6 (and above) from v.3.5.2. Older versions require at least Java 8.

You can find the latest released version [here](https://github.com/queueit/KnownUser.V3.JAVA/releases/latest).

## Installation
You can install this SDK in a couple of ways:
 - Clone the repository and use it as a dependency
 - Include the following maven dependency:
```xml
<dependency>
  <groupId>com.queue-it.connector</groupId>
  <artifactId>connector</artifactId>
  <version>3.6.2</version>
</dependency>
```

## Implementation
The KnownUser validation must be done on all requests except requests for static and cached pages, resources like images, css files and .... So, if you add the KnownUser validation logic to a central place, then be sure that the Triggers only fire on page requests (including ajax requests) and not on e.g. image. 

This example is using the *[IntegrationConfigProvider](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/IntegrationConfigProvider.java)* to download the queue configuration. The IntegrationConfigProvider.java file is an example of how the download and caching of the configuration can be done. This is just an example, but if you make your own downloader, please cache the result for 5 - 10 minutes to limit number of download requests. **You should NEVER download the configuration as part of the request handling**.

The following method is all that is needed to validate that a user has been through the queue:
```
    private void doValidation(HttpServletRequest request, HttpServletResponse response) {
        try {
            String customerId = "Your Queue-it customer ID";
            String secretKey = "Your 72 char secrete key as specified in Go Queue-it self-service platform";
	    String apiKey = "Your api-key as specified in Go Queue-it self-service platform";

            String queueitToken = request.getParameter(KnownUser.QueueITTokenKey);
            String pureUrl = getPureUrl(request);
	    
	    // The pureUrl is used to match Triggers and as the Target url (where to return the users to)
            // It is therefor important that the pureUrl is exactly the url of the users browsers. So if your webserver is 
            // e.g. behind a load balancer that modifies the host name or port, reformat the pureUrl before proceeding           
            CustomerIntegration integrationConfig = IntegrationConfigProvider.getCachedIntegrationConfig(customerId, apiKey);

            //Verify if the user has been through the queue
            RequestValidationResult validationResult = KnownUser.validateRequestByIntegrationConfig(
                    pureUrl, queueitToken, integrationConfig, customerId, request, response, secretKey);

            if (validationResult.doRedirect()) {
                //Adding no cache headers to prevent browsers to cache requests
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
                //end
                if (validationResult.isAjaxResult) {
                    //In case of ajax call send the user to the queue by sending a custom queue-it header and redirecting user to queue from javascript
                    response.setHeader(validationResult.getAjaxQueueRedirectHeaderKey(), validationResult.getAjaxRedirectUrl());
                } else {
                    //Send the user to the queue - either becuase hash was missing or becuase is was invalid
                    response.sendRedirect(validationResult.getRedirectUrl());
                }
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } else {
                String queryString = request.getQueryString();
                //Request can continue - we remove queueittoken form querystring parameter to avoid sharing of user specific token
		if (queryString != null && queryString.contains(KnownUser.QueueITTokenKey) && "Queue".equals(validationResult.getActionType()) ) {                
                    response.sendRedirect(pureUrl);
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                }
            }
        } catch (Exception ex) {
            // There was an error validating the request
            // Use your own logging framework to log the error
            // This was a configuration error, so we let the user continue
        }
    }
    
    // Helper method to get url without token.
    // It uses patterns which is unsupported in Java 6, so if you are using this version please reach out to us.
    private String getPureUrl(HttpServletRequest request){
        Pattern pattern = Pattern.compile("([\\?&])(" + KnownUser.QueueITTokenKey + "=[^&]*)", Pattern.CASE_INSENSITIVE);
        String queryString = request.getQueryString();
        String url = request.getRequestURL().toString() + (queryString != null ? ("?" + queryString) : "");

        String pureUrl = pattern.matcher(url).replaceAll("");
        return pureUrl;
    }
```

## Implementation using inline queue configuration
Specify the configuration in code without using the Trigger/Action paradigm. In this case it is important *only to queue-up page requests* and not requests for resources. This can be done by adding custom filtering logic before caling the KnownUser.resolveQueueRequestByLocalConfig() method.

The following is an example of how to specify the configuration in code:
 
```
    private void doValidationByLocalEventConfig(HttpServletRequest request, HttpServletResponse response) {
        try {
                       
            String customerId = "Your Queue-it customer ID";
            String secretKey = "Your 72 char secrete key as specified in Go Queue-it self-service platform";

            String queueitToken = request.getParameter(KnownUser.QueueITTokenKey);
            String pureUrl = getPureUrl(request);
            
            QueueEventConfig eventConfig = new QueueEventConfig();
            eventConfig.setEventId("event1"); //ID of the queue to use           
            //eventConfig.setCookieDomain(".mydomain.com"); //Optional - Domain name where the Queue-it session cookie should be saved. 
            eventConfig.setQueueDomain("queue.mydomain.com"); //Domain name of the queue. 
            eventConfig.setCookieValidityMinute(15); //Validity of the Queue-it session cookie should be positive number.
            eventConfig.setExtendCookieValidity(true); //Should the Queue-it session cookie validity time be extended each time the validation runs?
            //eventConfig.setCulture("en-US"); //Optional - Culture of the queue layout in the format specified here: https://msdn.microsoft.com/en-us/library/ee825488(v=cs.20).aspx. If unspecified then settings from Event will be used.
            //eventConfig.setLayoutName("MyCustomLayoutName"); //Optional - Name of the queue ticket layout.If unspecified then settings from Event will be used.
            
            //Verify if the user has been through the queue
            RequestValidationResult validationResult = KnownUser.resolveQueueRequestByLocalConfig(pureUrl, queueitToken, eventConfig, customerId, request, response, secretKey);

            if (validationResult.doRedirect()) {
                //Adding no cache headers to prevent browsers to cache requests
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
                //end
		 if (validationResult.isAjaxResult) {
                    //In case of ajax call send the user to the queue by sending a custom queue-it header and redirecting user to queue from javascript
                    response.setHeader(validationResult.getAjaxQueueRedirectHeaderKey(), validationResult.getAjaxRedirectUrl());
                } else {
                    //Send the user to the queue - either becuase hash was missing or becuase is was invalid
                    response.sendRedirect(validationResult.getRedirectUrl());
                }               
            } else {
                String queryString = request.getQueryString();
                //Request can continue - we remove queueittoken form querystring parameter to avoid sharing of user specific token
                if (queryString != null && queryString.contains(KnownUser.QueueITTokenKey) && validationResult.getActionType() == "Queue") {
                    response.sendRedirect(pureUrl);
                }
            }
        } catch (Exception ex) {
            // There was an error validating the request
            // Use your own logging framework to log the error
            // This was a configuration error, so we let the user continue     
        }
    }
```

## Extracting information from QueueITToken
When users are redirected back from queue-it website they carry a QueueITToken with some information which is used to validate their request by SDK. 
In specific cases you would like to validate, process or extract specfic parameters you can use QueueParameterHelper class in [KnownUserHelper.java](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/KnownUserHelper.java).
Calling *QueueParameterHelper.getIsTokenValid()* will validate the token and passing QueueITToken to *QueueParameterHelper.extractQueueParams* you will get a QueueUrlParams result containing all parameters found in the token.
