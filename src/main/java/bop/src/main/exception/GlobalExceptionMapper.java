/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.exception;

import bop.src.main.bean.ApiResponse;
import bop.src.main.config.ApplicationConstants;
import bop.src.main.util.LogHelper;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * 
 * @author Ashraf.M.Fahmawi
 */
@Provider
public class GlobalExceptionMapper
        implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        int httpStatus
                = determineHttpStatus(exception);

        String message
                = determineResponseMessage(
                        exception,
                        httpStatus
                );

        String errorCode
                = determineErrorCode(
                        exception,
                        httpStatus
                );

        /*
         * The response filter will add requestId
         * from ContainerRequestContext.
         */
        ApiResponse response
                = new ApiResponse(
                        ApplicationConstants.STATUS_FAILED,
                        message,
                        null,
                        null,
                        currentTimestamp()
                );

        LogHelper.logError(
                "Service exception. "
                + "HTTP Status: "
                + httpStatus
                + ", Error Code: "
                + errorCode
                + ", Error: "
                + safeTechnicalMessage(exception)
        );

        return Response.status(httpStatus)
                .entity(response)
                .header(
                        "X-Error-Code",
                        errorCode
                )
                .build();
    }

    private int determineHttpStatus(
            Throwable exception) {

        if (exception instanceof IllegalArgumentException) {
            return Response.Status.BAD_REQUEST
                    .getStatusCode();
        }

        if (exception instanceof SQLException) {
            return Response.Status.INTERNAL_SERVER_ERROR
                    .getStatusCode();
        }

        if (exception instanceof WebApplicationException) {

            WebApplicationException webException
                    = (WebApplicationException) exception;

            return webException
                    .getResponse()
                    .getStatus();
        }

        return Response.Status.INTERNAL_SERVER_ERROR
                .getStatusCode();
    }

    private String determineResponseMessage(
            Throwable exception,
            int httpStatus) {

        if (httpStatus >= 400
                && httpStatus < 500
                && exception.getMessage() != null
                && !exception.getMessage().trim().isEmpty()) {

            return exception.getMessage().trim();
        }

        /*
         * Do not return database or internal
         * technical errors to the client.
         */
        return "Unable to complete the operation";
    }

    private String determineErrorCode(
            Throwable exception,
            int httpStatus) {

        if (exception instanceof IllegalArgumentException) {
            return "INVALID_REQUEST";
        }

        if (exception instanceof SQLException) {
            return "DATABASE_ERROR";
        }

        if (httpStatus == 404) {
            return "RESOURCE_NOT_FOUND";
        }

        if (httpStatus == 409) {
            return "RESOURCE_CONFLICT";
        }

        return "BUSINESS_OPERATION_FAILED";
    }

    private String safeTechnicalMessage(
            Throwable exception) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage()
                        .trim()
                        .isEmpty()) {

            return "Unexpected error";
        }

        String message
                = exception.getMessage().trim();

        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }

        return message;
    }

    private String currentTimestamp() {

        return LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
    }
}