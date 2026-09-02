/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.util;

import bop.src.main.bean.HttpTextResult;
import bop.src.main.bean.LogFinishRequest;
import bop.src.main.bean.LogStartRequest;
import bop.src.main.bean.LogStartResponse;
import bop.src.main.bean.TokenValidationResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class CentralServicesClient {

    private final ObjectMapper objectMapper;
    private final String validationUrl;
    private final String loggingStartUrl;
    private final String loggingFinishUrl;

    public CentralServicesClient() {

        ConfigLoader configLoader
                = new ConfigLoader();

        this.objectMapper
                = new ObjectMapper();

        /*
     * FIXED:
     * Authentication is mandatory.
         */
        this.validationUrl
                = requireProperty(
                        configLoader,
                        "authentication.validate.url"
                );

        /*
     * FIXED:
     * Logging is optional and must never
     * stop the business request.
         */
        this.loggingStartUrl
                = optionalProperty(
                        configLoader,
                        "logging.start.url"
                );

        this.loggingFinishUrl
                = optionalProperty(
                        configLoader,
                        "logging.finish.url"
                );

        if (!isLoggingConfigured()) {

            System.err.println(
                    "Central logging is not configured. "
                    + "Business requests will continue without logging."
            );
        }
    }

    private String optionalProperty(
            ConfigLoader configLoader,
            String propertyName) {

        String value
                = configLoader.getProperty(propertyName);

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    public boolean isLoggingConfigured() {

        return loggingStartUrl != null
                && !loggingStartUrl.trim().isEmpty()
                && loggingFinishUrl != null
                && !loggingFinishUrl.trim().isEmpty();
    }

    public TokenValidationResponse validateToken(
            String token) throws Exception {

        String requestJson
                = objectMapper.writeValueAsString(
                        new TokenValidationRequestBody(token)
                );

        HttpTextResult result
                = HttpHelper.callApiText(
                        "POST",
                        validationUrl,
                        requestJson
                );

        if (result.getBody() == null
                || result.getBody().trim().isEmpty()) {

            throw new Exception(
                    "Authentication service returned an empty response. "
                    + "HTTP status: "
                    + result.getStatusCode()
            );
        }

        TokenValidationResponse response
                = objectMapper.readValue(
                        result.getBody(),
                        TokenValidationResponse.class
                );

        /*
         * Any non-2xx response is considered invalid.
         */
        if (!result.isSuccessful()) {
            response.setValid(false);
        }

        return response;
    }

    public LogStartResponse startLog(
            LogStartRequest request) throws Exception {

        if (!isLoggingConfigured()) {

            throw new IllegalStateException(
                    "Central logging service is not configured"
            );
        }

        String requestJson
                = objectMapper.writeValueAsString(request);

        HttpTextResult result
                = HttpHelper.callApiText(
                        "POST",
                        loggingStartUrl,
                        requestJson
                );

        if (!result.isSuccessful()) {

            throw new Exception(
                    "Logging start service failed. HTTP status: "
                    + result.getStatusCode()
                    + ", response: "
                    + result.getBody()
            );
        }

        if (result.getBody() == null
                || result.getBody().trim().isEmpty()) {

            throw new Exception(
                    "Logging start service returned an empty response"
            );
        }

        LogStartResponse response
                = objectMapper.readValue(
                        result.getBody(),
                        LogStartResponse.class
                );

        if (response.getStatus() != 0) {

            throw new Exception(
                    "Unable to start log: "
                    + response.getMessage()
            );
        }

        return response;
    }

    public void finishLog(
            LogFinishRequest request) throws Exception {

        if (!isLoggingConfigured()) {

            throw new IllegalStateException(
                    "Central logging service is not configured"
            );
        }

        String requestJson
                = objectMapper.writeValueAsString(request);

        HttpTextResult result
                = HttpHelper.callApiText(
                        "POST",
                        loggingFinishUrl,
                        requestJson
                );

        if (!result.isSuccessful()) {

            throw new Exception(
                    "Logging finish service failed. HTTP status: "
                    + result.getStatusCode()
                    + ", response: "
                    + result.getBody()
            );
        }
    }

    public String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    private String requireProperty(
            ConfigLoader configLoader,
            String propertyName) {

        String value
                = configLoader.getProperty(propertyName);

        if (value == null || value.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Missing configuration property: "
                    + propertyName
            );
        }

        return value.trim();
    }

    /*
     * Internal request body for /validate.
     */
    private static class TokenValidationRequestBody {

        private String token;

        public TokenValidationRequestBody() {
        }

        public TokenValidationRequestBody(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static String extractBearerToken(
            String authorizationHeader) {

        if (authorizationHeader == null
                || authorizationHeader
                        .trim()
                        .isEmpty()) {

            return null;
        }

        String prefix = "Bearer ";

        if (!authorizationHeader.regionMatches(
                true,
                0,
                prefix,
                0,
                prefix.length())) {

            return null;
        }

        String token
                = authorizationHeader
                        .substring(prefix.length())
                        .trim();

        return token.isEmpty() ? null : token;
    }

    public static String getOrCreateCorrelationId(HttpServletRequest httpServletRequest) {

        String correlationId = httpServletRequest.getHeader(
                "X-Correlation-ID"
        );

        if (correlationId == null
                || correlationId.trim().isEmpty()) {

            return UUID.randomUUID().toString();
        }

        return correlationId.trim();
    }

    public static String getSourceIp(HttpServletRequest httpServletRequest) {

        String forwardedFor
                = httpServletRequest.getHeader(
                        "X-Forwarded-For"
                );

        if (forwardedFor != null
                && !forwardedFor.trim().isEmpty()) {

            /*
             * The first address represents the original
             * client when trusted proxies are configured.
             */
            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return httpServletRequest
                .getRemoteAddr();
    }

    public static String safeErrorMessage(
            Exception exception) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage()
                        .trim()
                        .isEmpty()) {

            return "Unexpected service error";
        }

        String message
                = exception.getMessage().trim();

        /*
         * Prevent an excessively large error message.
         */
        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }

        return message;
    }

    public long getAuthUserId(HttpServletRequest httpRequest) {
        String token = CentralServicesClient.extractBearerToken(httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
        TokenValidationResponse tokenValidation = null;
        try {
            tokenValidation = this.validateToken(token);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long AuthUser = Long.parseLong(tokenValidation.getUserId());
        return AuthUser;
    }

}
