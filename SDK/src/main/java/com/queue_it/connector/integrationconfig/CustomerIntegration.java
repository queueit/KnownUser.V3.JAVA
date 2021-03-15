package com.queue_it.connector.integrationconfig;

public class CustomerIntegration {

    //sorted list of integrations
    public IntegrationConfigModel[] Integrations;
    public int Version;

    public CustomerIntegration() {
        Integrations = new IntegrationConfigModel[0];
        Version = -1;
    }
}
