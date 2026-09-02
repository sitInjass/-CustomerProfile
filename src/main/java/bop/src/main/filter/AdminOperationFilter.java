package bop.src.main.filter;

import bop.src.main.bean.ApiResponse;
import bop.src.main.bean.LogFinishRequest;
import bop.src.main.bean.LogStartRequest;
import bop.src.main.bean.LogStartResponse;
import bop.src.main.bean.TokenValidationResponse;
import bop.src.main.config.ApplicationConstants;
import bop.src.main.service.PermissionService;
import bop.src.main.util.CentralServicesClient;
import bop.src.main.util.LogHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Ashraf.M.Fahmawi
 */
@Priority(Priorities.AUTHENTICATION)
public class AdminOperationFilter
        implements ContainerRequestFilter,
        ContainerResponseFilter {

    private final String requiredPermission;
    private final String serviceName;
    private final PermissionService permissionService;

    public AdminOperationFilter(
            String requiredPermission,
            String serviceName) {

        this.requiredPermission
                = requiredPermission;

        this.serviceName
                = serviceName;

        this.permissionService
                = new PermissionService();
    }

    /*
     * Runs before the protected Resource method.
     */
    @Override
    public void filter(
            ContainerRequestContext requestContext)
            throws IOException {

        String requestId
                = UUID.randomUUID().toString();

        String correlationId
                = getOrCreateCorrelationId(
                        requestContext
                );

        saveRequestInformation(
                requestContext,
                requestId,
                correlationId
        );

        CentralServicesClient centralClient;

        try {
            centralClient = new CentralServicesClient();

        } catch (Exception exception) {
            LogHelper.logError(
                    "Central services configuration error. "
                    + "Request ID: "
                    + requestId
                    + ", Service: "
                    + serviceName
                    + ", Error: "
                    + safeMessage(exception)
            );

            abort(
                    requestContext,
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Service configuration error",
                    "SERVICE_CONFIGURATION_ERROR",
                    requestId
            );

            return;
        }

        /*
         * Token errors are printed locally only.
         * Central logging has not started yet.
         */
        String token
                = CentralServicesClient.extractBearerToken(
                        requestContext.getHeaderString(
                                HttpHeaders.AUTHORIZATION
                        )
                );

        if (token == null) {
            LogHelper.logWarning(
                    "Bearer token is missing. "
                    + "Request ID: "
                    + requestId
                    + ", Service: "
                    + serviceName
                    + ", Path: "
                    + getApiPath(requestContext)
            );

            abort(
                    requestContext,
                    Response.Status.UNAUTHORIZED,
                    "Bearer token is required",
                    "TOKEN_REQUIRED",
                    requestId
            );

            return;
        }

        TokenValidationResponse tokenValidation;

        try {
            /*
             * Existing token-validation mechanism.
             */
            tokenValidation
                    = centralClient.validateToken(token);

        } catch (Exception exception) {
            LogHelper.logError(
                    "Authentication service is unavailable. "
                    + "Request ID: "
                    + requestId
                    + ", Service: "
                    + serviceName
                    + ", Error: "
                    + safeMessage(exception)
            );

            abort(
                    requestContext,
                    Response.Status.SERVICE_UNAVAILABLE,
                    "Authentication service is unavailable",
                    "AUTH_SERVICE_UNAVAILABLE",
                    requestId
            );

            return;
        }

        if (!isValidActiveToken(tokenValidation)) {
            String validationMessage
                    = getValidationMessage(
                            tokenValidation
                    );

            LogHelper.logWarning(
                    "Token validation rejected. "
                    + "Request ID: "
                    + requestId
                    + ", Service: "
                    + serviceName
                    + ", Reason: "
                    + validationMessage
            );

            abort(
                    requestContext,
                    Response.Status.UNAUTHORIZED,
                    validationMessage,
                    "INVALID_TOKEN",
                    requestId
            );

            return;
        }

        String authenticatedUserId
                = tokenValidation.getUserId();

        requestContext.setProperty(
                ApplicationConstants.USER_ID_PROPERTY,
                authenticatedUserId
        );

        /*
         * Central logging starts only after successful
         * token validation and after userId is known.
         */
        boolean logStarted
                = startLog(
                        requestContext,
                        centralClient,
                        requestId,
                        correlationId,
                        authenticatedUserId
                );

        requestContext.setProperty(
                ApplicationConstants.LOG_STARTED_PROPERTY,
                logStarted
        );

        requestContext.setProperty(
                ApplicationConstants.LOG_CLIENT_PROPERTY,
                centralClient
        );

        /*
         * Permission is checked after logging starts.
         * Permission rejection will therefore be recorded.
         */
        try {
            boolean authorized
                    = permissionService.hasPermission(
                            authenticatedUserId,
                            ApplicationConstants.APPLICATION_NAME,
                            requiredPermission
                    );

            if (!authorized) {
                LogHelper.logWarning(
                        "Permission denied. "
                        + "Request ID: "
                        + requestId
                        + ", User ID: "
                        + authenticatedUserId
                        + ", Application: "
                        + ApplicationConstants.APPLICATION_NAME
                        + ", Permission: "
                        + requiredPermission
                );

                abort(
                        requestContext,
                        Response.Status.FORBIDDEN,
                        "You do not have permission "
                        + "to perform this operation",
                        "PERMISSION_DENIED",
                        requestId
                );

                return;
            }

        } catch (Exception exception) {
            LogHelper.logError(
                    "Permission validation failed. "
                    + "Request ID: "
                    + requestId
                    + ", User ID: "
                    + authenticatedUserId
                    + ", Permission: "
                    + requiredPermission
                    + ", Error: "
                    + safeMessage(exception)
            );

            abort(
                    requestContext,
                    Response.Status.SERVICE_UNAVAILABLE,
                    "Permission validation is temporarily unavailable",
                    "PERMISSION_CHECK_FAILED",
                    requestId
            );
        }
    }

    /*
     * Runs after the Resource method or after abortWith().
     */
    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext)
            throws IOException {

        addTrackingHeaders(
                requestContext,
                responseContext
        );

        /*
     * Convert successful and failed responses
     * to the standard ApiResponse format.
         */
        normalizeResponse(
                requestContext,
                responseContext
        );

        if (!isLogStarted(requestContext)) {
            return;
        }

        CentralServicesClient centralClient
                = getCentralClient(requestContext);

        if (centralClient == null) {
            return;
        }

        String requestId
                = getStringProperty(
                        requestContext,
                        ApplicationConstants.REQUEST_ID_PROPERTY
                );

        int httpStatus
                = responseContext.getStatus();

        String businessStatus
                = determineBusinessStatus(
                        httpStatus
                );

        String errorCode
                = getStringProperty(
                        requestContext,
                        ApplicationConstants.ERROR_CODE_PROPERTY
                );

        /*
     * Exceptions handled by GlobalExceptionMapper
     * return their error code as a response header.
         */
        if (isBlank(errorCode)) {
            Object headerValue
                    = responseContext
                            .getHeaders()
                            .getFirst("X-Error-Code");

            if (headerValue != null) {
                errorCode = String.valueOf(
                        headerValue
                );
            }
        }

        String errorMessage
                = getStringProperty(
                        requestContext,
                        ApplicationConstants.ERROR_MESSAGE_PROPERTY
                );

        if (httpStatus >= 400
                && isBlank(errorMessage)) {

            errorMessage
                    = extractResponseMessage(
                            responseContext
                    );
        }

        finishLog(
                centralClient,
                requestId,
                httpStatus,
                businessStatus,
                responseContext.getEntity(),
                errorCode,
                errorMessage
        );

        /*
     * Internal error code should not remain
     * as an HTTP response header.
         */
        responseContext
                .getHeaders()
                .remove("X-Error-Code");
    }

    private boolean startLog(
            ContainerRequestContext requestContext,
            CentralServicesClient centralClient,
            String requestId,
            String correlationId,
            String authenticatedUserId) {

        try {
            LogStartRequest logRequest
                    = new LogStartRequest();

            logRequest.setRequestId(requestId);
            logRequest.setParentRequestId(null);
            logRequest.setCorrelationId(correlationId);

            logRequest.setApplicationName(
                    ApplicationConstants.APPLICATION_NAME
            );

            logRequest.setCallerApplication(
                    ApplicationConstants.CALLER_APPLICATION
            );

            logRequest.setServiceName(serviceName);

            logRequest.setHttpMethod(
                    requestContext.getMethod()
            );

            logRequest.setApiPath(
                    getApiPath(requestContext)
            );

            logRequest.setUserId(
                    authenticatedUserId
            );

            logRequest.setSourceIp(
                    getSourceIp(requestContext)
            );

            /**
             * logRequest.setRequestBody( readAndRestoreRequestBody(
             * requestContext ) );*
             */
            logRequest.setRequestBody(
                    buildRequestPayload(
                            requestContext,
                            centralClient
                    )
            );

            LogStartResponse logResponse
                    = centralClient.startLog(logRequest);

            if (logResponse == null) {
                throw new Exception(
                        "Logging start returned no response"
                );
            }

            if (logResponse.getRequestId() != null
                    && !requestId.equals(
                            logResponse.getRequestId()
                    )) {

                throw new Exception(
                        "Logging service returned "
                        + "a different requestId"
                );
            }

            return true;

        } catch (Exception exception) {
            LogHelper.logError(
                    "Logging start failed; request will continue. "
                    + "Request ID: "
                    + requestId
                    + ", Service: "
                    + serviceName
                    + ", Error: "
                    + safeMessage(exception)
            );

            return false;
        }
    }

    private void finishLog(
            CentralServicesClient centralClient,
            String requestId,
            int httpStatus,
            String businessStatus,
            Object responseEntity,
            String errorCode,
            String errorMessage) {

        try {
            LogFinishRequest finishRequest
                    = new LogFinishRequest();

            finishRequest.setRequestId(requestId);
            finishRequest.setHttpStatus(httpStatus);
            finishRequest.setBusinessStatus(businessStatus);

            finishRequest.setResponseBody(
                    responseEntity == null
                            ? null
                            : centralClient.toJson(
                                    responseEntity
                            )
            );

            finishRequest.setErrorCode(errorCode);
            finishRequest.setErrorMessage(errorMessage);

            centralClient.finishLog(finishRequest);

        } catch (Exception exception) {
            LogHelper.logError(
                    "Logging finish failed; response "
                    + "will not be changed. "
                    + "Request ID: "
                    + requestId
                    + ", Service: "
                    + serviceName
                    + ", Error: "
                    + safeMessage(exception)
            );
        }
    }

    /*
     * Reads the request body for logging, then restores it
     * so Jersey can still convert it to the Request Bean.
     */
    private String readAndRestoreRequestBody(
            ContainerRequestContext requestContext)
            throws IOException {

        if (!requestContext.hasEntity()) {
            return null;
        }

        InputStream inputStream
                = requestContext.getEntityStream();

        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream outputStream
                = new ByteArrayOutputStream();

        byte[] buffer
                = new byte[4096];

        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(
                    buffer,
                    0,
                    bytesRead
            );
        }

        byte[] bodyBytes
                = outputStream.toByteArray();

        /*
         * Restore the stream for the Resource method.
         */
        requestContext.setEntityStream(
                new ByteArrayInputStream(bodyBytes)
        );

        if (bodyBytes.length == 0) {
            return null;
        }

        return new String(
                bodyBytes,
                StandardCharsets.UTF_8
        );
    }

    private void abort(
            ContainerRequestContext requestContext,
            Response.Status httpStatus,
            String message,
            String errorCode,
            String requestId) {

        requestContext.setProperty(
                ApplicationConstants.ERROR_CODE_PROPERTY,
                errorCode
        );

        requestContext.setProperty(
                ApplicationConstants.ERROR_MESSAGE_PROPERTY,
                message
        );

        ApiResponse response
                = new ApiResponse(
                        ApplicationConstants.STATUS_FAILED,
                        message,
                        null,
                        requestId,
                        currentTimestamp()
                );

        requestContext.abortWith(
                Response.status(httpStatus)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(response)
                        .header(
                                "X-Request-ID",
                                requestId
                        )
                        .build()
        );
    }

    private void saveRequestInformation(
            ContainerRequestContext requestContext,
            String requestId,
            String correlationId) {

        requestContext.setProperty(
                ApplicationConstants.REQUEST_ID_PROPERTY,
                requestId
        );

        requestContext.setProperty(
                ApplicationConstants.CORRELATION_ID_PROPERTY,
                correlationId
        );

        requestContext.setProperty(
                ApplicationConstants.LOG_STARTED_PROPERTY,
                false
        );
    }

    private void addTrackingHeaders(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) {

        String requestId
                = getStringProperty(
                        requestContext,
                        ApplicationConstants.REQUEST_ID_PROPERTY
                );

        String correlationId
                = getStringProperty(
                        requestContext,
                        ApplicationConstants.CORRELATION_ID_PROPERTY
                );

        if (!isBlank(requestId)) {
            responseContext.getHeaders().putSingle(
                    "X-Request-ID",
                    requestId
            );
        }

        if (!isBlank(correlationId)) {
            responseContext.getHeaders().putSingle(
                    "X-Correlation-ID",
                    correlationId
            );
        }
    }

    private boolean isValidActiveToken(
            TokenValidationResponse tokenValidation) {

        return tokenValidation != null
                && tokenValidation.isValid()
                && "ACTIVE".equalsIgnoreCase(
                        tokenValidation.getSessionStatus()
                );
    }

    private String getValidationMessage(
            TokenValidationResponse tokenValidation) {

        if (tokenValidation == null
                || isBlank(tokenValidation.getMessage())) {

            return "Invalid or inactive token";
        }

        return tokenValidation
                .getMessage()
                .trim();
    }

    private String getOrCreateCorrelationId(
            ContainerRequestContext requestContext) {

        String correlationId
                = requestContext.getHeaderString(
                        "X-Correlation-ID"
                );

        if (isBlank(correlationId)) {
            return UUID.randomUUID().toString();
        }

        return correlationId.trim();
    }

    private String getApiPath(
            ContainerRequestContext requestContext) {

        String relativePath
                = requestContext
                        .getUriInfo()
                        .getPath();

        return "/services/"
                + relativePath;
    }

    private String getSourceIp(
            ContainerRequestContext requestContext) {

        String forwardedFor
                = requestContext.getHeaderString(
                        "X-Forwarded-For"
                );

        if (!isBlank(forwardedFor)) {
            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return null;
    }

    private String determineBusinessStatus(
            int httpStatus) {

        if (httpStatus >= 200
                && httpStatus < 300) {

            return ApplicationConstants.BUSINESS_STATUS_SUCCESS;
        }

        if (httpStatus >= 400
                && httpStatus < 500) {

            return ApplicationConstants.BUSINESS_STATUS_REJECTED;
        }

        return ApplicationConstants.BUSINESS_STATUS_FAILED;
    }

    private String extractResponseMessage(
            ContainerResponseContext responseContext) {

        Object entity
                = responseContext.getEntity();

        if (entity instanceof ApiResponse) {
            return ((ApiResponse) entity)
                    .getMessage();
        }

        return responseContext
                .getStatusInfo()
                .getReasonPhrase();
    }

    private boolean isLogStarted(
            ContainerRequestContext requestContext) {

        Object value
                = requestContext.getProperty(
                        ApplicationConstants.LOG_STARTED_PROPERTY
                );

        return Boolean.TRUE.equals(value);
    }

    private CentralServicesClient getCentralClient(
            ContainerRequestContext requestContext) {

        Object value
                = requestContext.getProperty(
                        ApplicationConstants.LOG_CLIENT_PROPERTY
                );

        if (value instanceof CentralServicesClient) {
            return (CentralServicesClient) value;
        }

        return null;
    }

    private String getStringProperty(
            ContainerRequestContext requestContext,
            String propertyName) {

        Object value
                = requestContext.getProperty(
                        propertyName
                );

        return value == null
                ? null
                : String.valueOf(value);
    }

    private String currentTimestamp() {

        return LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
    }

    private String safeMessage(
            Exception exception) {

        if (exception == null
                || isBlank(exception.getMessage())) {

            return "Unexpected error";
        }

        String message
                = exception.getMessage().trim();

        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }

        return message;
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    private void normalizeResponse(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) {

        String requestId
                = getStringProperty(
                        requestContext,
                        ApplicationConstants.REQUEST_ID_PROPERTY
                );

        Object currentEntity
                = responseContext.getEntity();

        /*
     * abort() and GlobalExceptionMapper already
     * return ApiResponse objects.
         */
        if (currentEntity instanceof ApiResponse) {

            ApiResponse apiResponse
                    = (ApiResponse) currentEntity;

            if (isBlank(apiResponse.getRequestId())) {
                apiResponse.setRequestId(requestId);
            }

            if (isBlank(apiResponse.getTimestamp())) {
                apiResponse.setTimestamp(
                        currentTimestamp()
                );
            }

            return;
        }

        int httpStatus
                = responseContext.getStatus();

        /*
     * Successful endpoint response.
         */
        if (httpStatus == Response.Status.NO_CONTENT
                .getStatusCode()) {
            return;
        }

        if (httpStatus >= 200
                && httpStatus < 300) {

            ApiResponse successResponse
                    = new ApiResponse(
                            ApplicationConstants.STATUS_SUCCESS,
                            "Success",
                            currentEntity,
                            requestId,
                            currentTimestamp()
                    );

            responseContext.setEntity(
                    successResponse,
                    null,
                    MediaType.APPLICATION_JSON_TYPE
            );

            return;
        }

        /*
     * Fallback for an error response not created
     * by abort() or GlobalExceptionMapper.
         */
        ApiResponse errorResponse
                = new ApiResponse(
                        ApplicationConstants.STATUS_FAILED,
                        responseContext
                                .getStatusInfo()
                                .getReasonPhrase(),
                        null,
                        requestId,
                        currentTimestamp()
                );

        responseContext.setEntity(
                errorResponse,
                null,
                MediaType.APPLICATION_JSON_TYPE
        );
    }

    private String buildRequestPayload(
            ContainerRequestContext requestContext,
            CentralServicesClient centralClient)
            throws Exception {

        if ("GET".equalsIgnoreCase(
                requestContext.getMethod())) {

            if (requestContext
                    .getUriInfo()
                    .getQueryParameters()
                    .isEmpty()) {

                return null;
            }

            return centralClient.toJson(
                    requestContext
                            .getUriInfo()
                            .getQueryParameters()
            );
        }

    
        return readAndRestoreRequestBody(
                requestContext
        );
    }

}
