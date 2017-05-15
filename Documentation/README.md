# Help and examples
This folder contains some extra helper functions and examples.


## Downloading the Integration Configuration
The KnownUser library needs the Triggers and Actions to know which pages to protect and which queues to use. 
These Triggers and Actions are specified in the Go Queue-it self-service portal.

The [IntegrationConfigProvider.cs](https://github.com/queueit/KnownUser.V3.JAVA/blob/master/Documentation/IntegrationConfigProvider.java) file is an example of how 
the download and caching of the configuration can be done. 
*This is just an example*, but if you make your own downloader, please cache the result for 5 minutes to limit number of download requests.

![Configuration Provider flow](https://github.com/queueit/KnownUser.V3.Java/blob/master/Documentation/ConfigurationProviderExample.PNG)
