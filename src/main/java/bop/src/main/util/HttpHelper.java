
package bop.src.main.util;

/**
 *
 * @author ِAshraf.M.Fahmawi
 */

import bop.src.main.bean.HttpTextResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public final class HttpHelper {

    private static final ConfigLoader CONFIG =
            new ConfigLoader();

    private static final int CONNECT_TIMEOUT_MS =
            getIntegerProperty(
                    "http.connect.timeout.ms",
                    5000
            );

    private static final int CONNECTION_REQUEST_TIMEOUT_MS =
            getIntegerProperty(
                    "http.connection.request.timeout.ms",
                    5000
            );

    private static final int READ_TIMEOUT_MS =
            getIntegerProperty(
                    "http.read.timeout.ms",
                    10000
            );

    private static final int MAX_RETRIES =
            getIntegerProperty(
                    "http.max.retries",
                    1
            );

    private static final int INITIAL_BACKOFF_SECONDS =
            getIntegerProperty(
                    "http.initial.backoff.seconds",
                    2
            );

    private static final boolean TRUST_ALL_SSL =
            getBooleanProperty(
                    "http.ssl.trust.all",
                    false
            );

    private HttpHelper() {
        // Utility class; prevent object creation.
    }

    /*
     * Simple call without authentication or additional headers.
     */
    public static HttpTextResult callApiText(
            String method,
            String apiUrl,
            String jsonPayload) {

        return callApiText(
                method,
                apiUrl,
                jsonPayload,
                null,
                null,
                null
        );
    }

    /*
     * Call using Basic Authentication.
     */
    public static HttpTextResult callApiText(
            String method,
            String apiUrl,
            String jsonPayload,
            String username,
            String password) {

        return callApiText(
                method,
                apiUrl,
                jsonPayload,
                username,
                password,
                null
        );
    }

    /*
     * General method supporting Basic Authentication
     * and additional HTTP headers.
     */
    public static HttpTextResult callApiText(
            String method,
            String apiUrl,
            String jsonPayload,
            String username,
            String password,
            Map<String, String> additionalHeaders) {

        if (apiUrl == null || apiUrl.trim().isEmpty()) {

            return networkErrorResult(
                    "API URL is required"
            );
        }

        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {

            attempt++;

            try (
                CloseableHttpClient client =
                        buildHttpClient();

                CloseableHttpResponse response =
                        executeRequest(
                                client,
                                method,
                                apiUrl,
                                jsonPayload,
                                username,
                                password,
                                additionalHeaders
                        )
            ) {

                int httpStatus =
                        response.getStatusLine()
                                .getStatusCode();

                String reasonPhrase =
                        response.getStatusLine()
                                .getReasonPhrase();

                String responseBody =
                        readResponseBody(response);

                MultivaluedMap<String, Object>
                        responseHeaders =
                        toJaxRsHeaders(
                                response.getAllHeaders()
                        );

                if (httpStatus >= 500
                        && attempt < MAX_RETRIES) {

                    waitBeforeRetry(attempt);
                    continue;
                }

                return new HttpTextResult(
                        httpStatus,
                        reasonPhrase,
                        responseBody,
                        responseHeaders
                );

            } catch (Exception exception) {

                lastException = exception;
                if (attempt < MAX_RETRIES) {

                    try {
                        waitBeforeRetry(attempt);

                    } catch (InterruptedException interrupted) {

                        Thread.currentThread().interrupt();

                        return networkErrorResult(
                                "HTTP call was interrupted"
                        );
                    }
                }
            }
        }

        String errorMessage =
                lastException == null
                ? "HTTP call failed after retries"
                : lastException.getMessage();

      

        return networkErrorResult(
                "Network error or retry attempts exhausted"
        );
    }

    /*
     * Convenience method for Bearer Token calls.
     */
    public static HttpTextResult callApiWithBearerToken(
            String method,
            String apiUrl,
            String jsonPayload,
            String bearerToken) {

        Map<String, String> headers =
                new HashMap<String, String>();

        if (bearerToken != null
                && !bearerToken.trim().isEmpty()) {

            headers.put(
                    "Authorization",
                    "Bearer " + bearerToken.trim()
            );
        }

        return callApiText(
                method,
                apiUrl,
                jsonPayload,
                null,
                null,
                headers
        );
    }

    private static CloseableHttpResponse executeRequest(
            CloseableHttpClient client,
            String method,
            String apiUrl,
            String jsonPayload,
            String username,
            String password,
            Map<String, String> additionalHeaders)
            throws Exception {

        HttpRequestBase request =
                buildRequest(
                        method,
                        apiUrl,
                        jsonPayload
                );

        /*
         * Basic Authentication is added only when
         * username or password is provided.
         */
        if (username != null || password != null) {

            String rawCredentials =
                    valueOrEmpty(username)
                    + ":"
                    + valueOrEmpty(password);

            String encodedCredentials =
                    Base64.getEncoder().encodeToString(
                            rawCredentials.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            request.setHeader(
                    "Authorization",
                    "Basic " + encodedCredentials
            );
        }

        request.setHeader(
                "Accept",
                "application/json"
        );

        if (request
                instanceof HttpEntityEnclosingRequestBase) {

            request.setHeader(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );
        }

        /*
         * Additional headers can override existing ones.
         */
        if (additionalHeaders != null) {

            for (Map.Entry<String, String> entry
                    : additionalHeaders.entrySet()) {

                if (entry.getKey() != null
                        && entry.getValue() != null) {

                    request.setHeader(
                            entry.getKey(),
                            entry.getValue()
                    );
                }
            }
        }

        return client.execute(request);
    }

    private static CloseableHttpClient buildHttpClient()
            throws Exception {

        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectTimeout(
                                CONNECT_TIMEOUT_MS
                        )
                        .setConnectionRequestTimeout(
                                CONNECTION_REQUEST_TIMEOUT_MS
                        )
                        .setSocketTimeout(
                                READ_TIMEOUT_MS
                        )
                        .setRedirectsEnabled(true)
                        .build();

        HttpClientBuilder builder =
                HttpClients.custom()
                        .setDefaultRequestConfig(
                                requestConfig
                        )
                        .disableCookieManagement()
                        /*
                         * Retries are handled manually
                         * by this helper.
                         */
                        .setRetryHandler(
                                new DefaultHttpRequestRetryHandler(
                                        0,
                                        false
                                )
                        )
                        .useSystemProperties();

        SSLContext sslContext;

        if (TRUST_ALL_SSL) {

            sslContext =
                    SSLContextBuilder.create()
                            .loadTrustMaterial(
                                    null,
                                    (certificateChain,
                                     authenticationType) -> true
                            )
                            .build();

            SSLConnectionSocketFactory sslFactory =
                    createSslSocketFactory(
                            sslContext,
                            true
                    );

            builder.setSSLSocketFactory(
                    sslFactory
            );

        } else {

            /*
             * Uses the JVM truststore and performs
             * normal certificate and hostname validation.
             */
            sslContext =
                    SSLContexts.createSystemDefault();

            SSLConnectionSocketFactory sslFactory =
                    createSslSocketFactory(
                            sslContext,
                            false
                    );

            builder.setSSLSocketFactory(
                    sslFactory
            );
        }

        return builder.build();
    }

    private static SSLConnectionSocketFactory
            createSslSocketFactory(
                    SSLContext sslContext,
                    boolean trustAll) {

        String[] preferredProtocols =
                new String[] {
                    "TLSv1.3",
                    "TLSv1.2"
                };

        Set<String> supportedProtocols =
                new HashSet<String>(
                        Arrays.asList(
                                sslContext
                                        .getSupportedSSLParameters()
                                        .getProtocols()
                        )
                );

        List<String> enabledProtocols =
                new ArrayList<String>();

        for (String protocol : preferredProtocols) {

            if (supportedProtocols.contains(protocol)) {
                enabledProtocols.add(protocol);
            }
        }

        /*
         * Java 8 normally supports TLSv1.2.
         */
        if (enabledProtocols.isEmpty()) {
            enabledProtocols.add("TLSv1.2");
        }

        if (trustAll) {

            return new SSLConnectionSocketFactory(
                    sslContext,
                    enabledProtocols.toArray(
                            new String[enabledProtocols.size()]
                    ),
                    null,
                    NoopHostnameVerifier.INSTANCE
            );
        }

        return new SSLConnectionSocketFactory(
                sslContext,
                enabledProtocols.toArray(
                        new String[enabledProtocols.size()]
                ),
                null,
                SSLConnectionSocketFactory
                        .getDefaultHostnameVerifier()
        );
    }

    private static HttpRequestBase buildRequest(
            String method,
            String apiUrl,
            String jsonPayload)
            throws Exception {

        String normalizedMethod =
                method == null
                ? "GET"
                : method.trim()
                        .toUpperCase(Locale.ROOT);

        switch (normalizedMethod) {

            case "POST": {

                HttpPost request =
                        new HttpPost(
                                new URIBuilder(apiUrl).build()
                        );

                setJsonEntity(
                        request,
                        jsonPayload
                );

                return request;
            }

            case "PUT": {

                HttpPut request =
                        new HttpPut(
                                new URIBuilder(apiUrl).build()
                        );

                setJsonEntity(
                        request,
                        jsonPayload
                );

                return request;
            }

            case "PATCH": {

                HttpPatch request =
                        new HttpPatch(
                                new URIBuilder(apiUrl).build()
                        );

                setJsonEntity(
                        request,
                        jsonPayload
                );

                return request;
            }

            case "DELETE":

                return new HttpDelete(
                        new URIBuilder(apiUrl).build()
                );

            case "GET":

                return new HttpGet(
                        new URIBuilder(apiUrl).build()
                );

            default:

                throw new IllegalArgumentException(
                        "Unsupported HTTP method: "
                        + normalizedMethod
                );
        }
    }

    private static void setJsonEntity(
            HttpEntityEnclosingRequestBase request,
            String jsonPayload) {

        String payload =
                jsonPayload == null
                ? ""
                : jsonPayload;

        StringEntity entity =
                new StringEntity(
                        payload,
                        ContentType.APPLICATION_JSON
                );

        request.setEntity(entity);
    }

    private static String readResponseBody(
            CloseableHttpResponse response)
            throws Exception {

        HttpEntity entity =
                response.getEntity();

        if (entity == null) {
            return null;
        }

        return EntityUtils.toString(
                entity,
                StandardCharsets.UTF_8
        );
    }

    private static MultivaluedMap<String, Object>
            toJaxRsHeaders(Header[] headers) {

        MultivaluedMap<String, Object> result =
                new MultivaluedHashMap<String, Object>();

        if (headers != null) {

            for (Header header : headers) {

                result.add(
                        header.getName(),
                        header.getValue()
                );
            }
        }

        return result;
    }

    private static void waitBeforeRetry(
            int attempt)
            throws InterruptedException {

        long backoffSeconds =
                INITIAL_BACKOFF_SECONDS
                * (1L << (attempt - 1));

        TimeUnit.SECONDS.sleep(
                backoffSeconds
        );
    }

    private static HttpTextResult networkErrorResult(
            String message) {

        return new HttpTextResult(
                599,
                message,
                null,
                new MultivaluedHashMap<String, Object>()
        );
    }

    private static int getIntegerProperty(
            String propertyName,
            int defaultValue) {

        try {

            String value =
                    CONFIG.getProperty(propertyName);

            if (value == null
                    || value.trim().isEmpty()) {

                return defaultValue;
            }

            return Integer.parseInt(
                    value.trim()
            );

        } catch (Exception exception) {

            return defaultValue;
        }
    }

    private static boolean getBooleanProperty(
            String propertyName,
            boolean defaultValue) {

        try {

            String value =
                    CONFIG.getProperty(propertyName);

            if (value == null
                    || value.trim().isEmpty()) {

                return defaultValue;
            }

            return Boolean.parseBoolean(
                    value.trim()
            );

        } catch (Exception exception) {

            return defaultValue;
        }
    }

    private static String valueOrEmpty(
            String value) {

        return value == null ? "" : value;
    }
}
