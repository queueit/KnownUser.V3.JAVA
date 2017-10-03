>You can fínd the latest released version [here](https://github.com/queueit/KnownUser.V3.JAVA/releases/latest).

# Queue-it KnownUser SDK for Java 
The Queue-it Security Framework is used to ensure that end users cannot bypass the queue by adding a server-side integration to your server. 
## Introduction
When a user is redirected back from the queue to your website, the queue engine can attache a query string parameter (`queueittoken`) containing some information about the user. 
The most important fields of the `queueittoken` are:

 - q - the users unique queue identifier
 - ts - a timestamp of how long this redirect is valid
 - h - a hash of the token


The high level logic is as follows:

![The KnownUser validation flow](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/KnownUserFlow.png)

 1. User requests a page on your server
 2. The validation method sees that the has no Queue-it session cookie and no `queueittoken` and sends him to the correct queue based on the configuration
 3. User waits in the queue
 4. User is redirected back to your website, now with a `queueittoken`
 5. The validation method validates the `queueittoken` and creates a Queue-it session cookie
 6. The user browses to a new page and the Queue-it session cookie will let him go there without queuing again

## How to validate a user
To validate that the current user is allowed to enter your website (has been through the queue) these steps are needed:

 1. Providing the queue configuration to the KnownUser validation
 2. Validate the `queueittoken` and store a session cookie


### 1. Providing the queue configuration
The recommended way is to use the Go Queue-it self-service portal to setup the configuration. 
The configuration specifies a set of Triggers and Actions. A Trigger is an expression matching one, more or all URLs on your website. 
When a user enter your website and the URL matches a Trigger-expression the corresponding Action will be triggered. 
The Action specifies which queue the users should be send to. 
In this way you can specify which queue(s) should protect which page(s) on the fly without changing the server-side integration.

This configuration can then be downloaded to your application server as shown in the *[IntegrationConfigProvider](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/IntegrationConfigProvider.java)* example. 
The configuration will be downloaded and cached for 5 minutes. 

### 2. Validate the `queueittoken` and store a session cookie
To validate that the user has been through the queue, use the `KnownUser.validateRequestByIntegrationConfig()` method. 
This call will validate the timestamp and hash and if valid create a "QueueITAccepted-SDFrts345E-V3_[EventId]" cookie with a TTL as specified in the configuration.
If the timestamp or hash is invalid, the user is send back to the queue.


## Implementation
The KnownUser validation must *only* be done on *page requests*. 

This example is using the *[IntegrationConfigProvider](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/IntegrationConfigProvider.java)* to download the queue configuration. 

The following method is all that is needed to validate that a user has been through the queue:
```
    private void doValidation(HttpServletRequest request, HttpServletResponse response) {
        try {
        
            //Adding no cache headers to prevent browsers to cache requests
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0
            response.setDateHeader("Expires", 0); // Proxies.
            //end
            
            String customerId = "Your Queue-it customer ID";
            String secretKey = "Your 72 char secrete key as specified in Go Queue-it self-service platform";

            String queueitToken = request.getParameter(KnownUser.QueueITTokenKey);
            String pureUrl = getPureUrl(request);
            // The pureUrl is used to match Triggers and as the Target url (where to return the users to)
            // It is therefor important that the pureUrl is exactly the url of the users browsers. So if your webserver is 
            // e.g. behind a load balancer that modifies the host name or port, reformat the pureUrl before proceeding
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
    
    // Helper method
    private String getPureUrl(HttpServletRequest request){
        Pattern pattern = Pattern.compile("([\\?&])(" + KnownUser.QueueITTokenKey + "=[^&]*)", Pattern.CASE_INSENSITIVE);
        String queryString = request.getQueryString();
        String url = request.getRequestURL().toString() + (queryString != null ? ("?" + queryString) : "");

        String pureUrl = pattern.matcher(url).replaceAll("");
        return pureUrl;
    }
```


## Alternative Implementation
If your application server (maybe due to security reasons) is not allowed to do external GET requests, then you have three options:

1. Manually download the configuration file from Queue-it Go self-service portal, save it on your application server and load it from local disk
2. Use an internal gateway server to download the configuration file and save to application server
3. Specify the configuration in code without using the Trigger/Action paradigm. In this case it is important *only to queue-up page requests* and not requests for resources or AJAX calls. 


The following is an example of how to specify the configuration in code:
 
```
    private void doValidationByLocalEventConfig(HttpServletRequest request, HttpServletResponse response) {
        try {
 
            //Adding no cache headers to prevent browsers to cache requests
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0
            response.setDateHeader("Expires", 0); // Proxies.
            //end
                         
            String customerId = "Your Queue-it customer ID";
            String secretKey = "Your 72 char secrete key as specified in Go Queue-it self-service platform";

            String queueitToken = request.getParameter(KnownUser.QueueITTokenKey);
            String pureUrl = getPureUrl(request);
            
            QueueEventConfig eventConfig = new QueueEventConfig();
            eventConfig.setEventId("event1"); //ID of the queue to use           
            eventConfig.setCookieDomain(".mydomain.com"); //Optional - Domain name where the Queue-it session cookie should be saved. Default is to save on the domain of the request
            eventConfig.setQueueDomain("queue.mydomain.com"); //Optional - Domian name of the queue. Default is [CustomerId].queue-it.net
            eventConfig.setCookieValidityMinute(15); //Optional - Validity of the Queue-it session cookie. Default is 10 minutes
            eventConfig.setExtendCookieValidity(false); //Optional - Should the Queue-it session cookie validity time be extended each time the validation runs? Default is true.
            eventConfig.setCulture("en-US"); //Optional - Culture of the queue ticket layout in the format specified here: https://msdn.microsoft.com/en-us/library/ee825488(v=cs.20).aspx Default is to use what is specified on Event
            eventConfig.setLayoutName("MyCustomLayoutName"); //Optional - Name of the queue ticket layout - e.g. "Default layout by Queue-it". Default is to use what is specified on the Event
            
            //Verify if the user has been through the queue
            RequestValidationResult validationResult = KnownUser.resolveQueueRequestByLocalConfig(pureUrl, queueitToken, eventConfig, customerId, request, response, secretKey);

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
```
