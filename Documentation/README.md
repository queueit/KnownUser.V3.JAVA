# Help and examples
This folder contains some extra helper functions and examples.


## Downloading the Integration Configuration
The KnownUser library needs the Triggers and Actions to know which pages to protect and which queues to use. 
These Triggers and Actions are specified in the Go Queue-it self-service portal.

![Configuration Provider flow](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/ConfigProviderExample.png)

There are 2 possible ways you can retrieve the integration config information and provide it for the Known User SDK:

**1. Time based pulling:**
   In this method, you would have a long running tasks retrieving the latest version of published integration with a sepecified time interval from Queue-it repository with the address **https://[your-customer-id].queue-it.net/status/integrationconfig/secure/[your-customer-id]** then cache and reuse the retrieved value until the next interval. **To prevent unauthorized access to your config file make sure you enable “Secure integration config” setting in the Go Queue-it self-service portal** then provide your API key in the request header to retrieve integration config.

The [IntegrationConfigProvider.java](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/IntegrationConfigProvider.java) file is an example of how 
the download and caching of the configuration can be done. 
*This is just an example*, but if you make your own downloader, please cache the result for 5 - 10 minutes to limit number of download requests. You should NEVER download the configuration as part of the request handling.

**2. Manually updating integration configuration:**
    In this method, after changing and publishing your configuration using the Go Queue-it self-service portal, you are able to download the file and then manually copy and paste it to your intfrastructure. You can find your configuration file here **https://[your-customer-id].queue-it.net/status/integrationconfig/[your-customer-id]** or via secure link (*) **https://[your-customer-id].queue-it.net/status/integrationconfig/secure/[your-customer-id]** after a successful publish.

##### * How to download integration config with Api Key:
   Integration configuration contains valuable information like triggers and actions. Anyone can download the configuration by knowing the URL because it does not require any authentication. You can protect integration configurations by enabling the “**Secure integration config**” setting, so only legitimate systems can download it by providing a valid API key.

   1. You need to enable “**Secure integration config**” setting in the Go Queue-it self-service portal.
   2. You need to decorate the request by adding API key in the request header. You can get API key in the Go Queue-it self-service portal.
   3. Remember to add Host header in the request if your framework doesn't do that automatically. A missing host header will result in a 400 Bad Request response.
   
curl --request GET https://[your-customer-id].queue-it.net/status/integrationconfig/secure/[your-customer-id] --header 'api-key: [Customer API-Key]' --header 'Host: queue-it.net'


## Extracting information from QueueITToken
When users are redirected back from queue-it website they carry a QueueITToken with some information which is used to validate their request by SDK. 
In specific cases you would like to validate, process or extract specfic parameters you can use QueueParameterHelper class in [KnownUserHelper.java](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/KnownUserHelper.java).
Calling *QueueParameterHelper.getIsTokenValid()* will validate the token and passing QueueITToken to *QueueParameterHelper.extractQueueParams* you will get a QueueUrlParams result containing all parameters found in the token. 
