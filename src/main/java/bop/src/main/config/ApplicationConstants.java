/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.config;

/**
 *
 * @author ِAshraf.M.Fahmawi
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
    }

    /*
     * CHANGE ONCE PER WAR.
     * Must match ADM_PERMISSIONS.APPLICATION_NAME.
     */
    public static final String APPLICATION_NAME = "UserManagementAdminPortal";

   
    public static final String CALLER_APPLICATION = "AdminPortal";

    /*
     * Request context properties.
     */
    public static final String REQUEST_ID_PROPERTY = "admin.requestId";

    public static final String CORRELATION_ID_PROPERTY = "admin.correlationId";

    public static final String USER_ID_PROPERTY = "admin.authenticatedUserId";

    public static final String LOG_STARTED_PROPERTY = "admin.logStarted";

    public static final String LOG_CLIENT_PROPERTY = "admin.loggingClient";

    public static final String ERROR_CODE_PROPERTY = "admin.errorCode";

    public static final String ERROR_MESSAGE_PROPERTY = "admin.errorMessage";

    /*
     * Standard application statuses.
     */
    public static final int STATUS_SUCCESS = 0;

    public static final int STATUS_FAILED = -1;

    /*
     * Values allowed by ADM_API_LOG.BUSINESS_STATUS.
     */
    public static final String BUSINESS_STATUS_SUCCESS = "SUCCESS";

    public static final String BUSINESS_STATUS_FAILED = "FAILED";

    public static final String BUSINESS_STATUS_REJECTED = "REJECTED";
}
