package queueit.knownuserv3.sdk;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import queueit.knownuserv3.sdk.integrationconfig.CustomerIntegration;

final class IntegrationConfigProvider {

    private static final int DOWNLOAD_TIMEOUT_MS = 4000;
    static final long REFRESH_INTERVALS_MS = 5 * 60 * 1000;
    static final double RETRY_EXCEPTION_SLEEPS = 5;
    private static final Timer timer = new Timer();
    private static final Object LOCK_OBJECT = new Object();
    static CustomerIntegration cachedIntegrationConfig;
    private static boolean isInitialized = false;
    private static String customerId;

    public static CustomerIntegration getCachedIntegrationConfig(String customerId) {
        if (!isInitialized) {
            IntegrationConfigProvider.customerId = customerId;
            synchronized (LOCK_OBJECT) {
                if (!isInitialized) {
                    refreshCache(true);

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            refreshCache(false);
                        }
                    }, REFRESH_INTERVALS_MS, REFRESH_INTERVALS_MS);

                    isInitialized = true;
                }
            }
        }
        return cachedIntegrationConfig;
    }

    private static void refreshCache(boolean init) {
        int tryCount = 0;

        while (tryCount < 5) {
            long timeBaseQueryString = (System.currentTimeMillis() / 1000L);            
            String configUrl = String.format("https://%s.queue-it.net/status/integrationconfig/%s?qr=%s", customerId, customerId, timeBaseQueryString);

            try {
                String jsonText = getJsonText(configUrl);
                cachedIntegrationConfig = new Gson().fromJson(jsonText, CustomerIntegration.class);
                return;
            } catch (IOException ex) {
                ++tryCount;
                if (tryCount >= 5) {
                    //Use your favorit logging framework to log the exceptoin
                    break;
                }

                double sleepTime = ((!init) ? RETRY_EXCEPTION_SLEEPS : (0.200 * tryCount)) * 1000;
                try {
                    Thread.sleep((long) sleepTime);
                } catch (InterruptedException ex1) {
                    //Use your favorit logging framework to log the exceptoin
                }
            }
        }
    }

    private static String getJsonText(String url) throws IOException {
        URL resource = new URL(url);
        URLConnection connection = resource.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        return response.toString();
    }
}
