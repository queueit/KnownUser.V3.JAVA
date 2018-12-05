# Help and examples
This folder contains some extra helper functions and examples.


## Downloading the Integration Configuration
The KnownUser library needs the Triggers and Actions to know which pages to protect and which queues to use. 
These Triggers and Actions are specified in the Go Queue-it self-service portal.

The [IntegrationConfigProvider.java]
(https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/IntegrationConfigProvider.java) file is an example of how 
the download and caching of the configuration can be done. 
*This is just an example*, but if you make your own downloader, please cache the result for 5 - 10 minutes to limit number of download requests. You should NEVER download the configuration as part of the request handling.

![Configuration Provider flow](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/ConfigProviderExample.png)

## Extracting information from QueueITToken
When users are redirected back from queue-it website they carry a QueueITToken with some information which is used to validate their request by SDK. 
In specific cases you would like to validate, process or extract specfic parameters you can use QueueParameterHelper class in [KnownUserHelper.java](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/KnownUserHelper.java).
Calling *QueueParameterHelper.getIsTokenValid()* will validate the token and passing QueueITToken to *QueueParameterHelper.extractQueueParams* you will get a QueueUrlParams result containing all parameters found in the token. 
