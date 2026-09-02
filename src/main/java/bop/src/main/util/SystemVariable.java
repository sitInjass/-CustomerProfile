/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.util;

/**
 *
 * @author ِAshraf.M.Fahmawi
 */
public class SystemVariable {


    public static final String INSERT_LOG_SQL
            = "INSERT INTO ADM_API_LOG ("
            + "REQUEST_ID, "
            + "PARENT_REQUEST_ID, "
            + "CORRELATION_ID, "
            + "APPLICATION_NAME, "
            + "CALLER_APPLICATION, "
            + "SERVICE_NAME, "
            + "HTTP_METHOD, "
            + "API_PATH, "
            + "USER_ID, "
            + "SOURCE_IP, "
            + "REQUEST_BODY, "
            + "REQUEST_TIME, "
            + "BUSINESS_STATUS"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_LOG_SQL
            = "UPDATE ADM_API_LOG SET "
            + "RESPONSE_BODY = ?, "
            + "RESPONSE_TIME = ?, "
            + "HTTP_STATUS = ?, "
            + "BUSINESS_STATUS = ?, "
            + "ERROR_CODE = ?, "
            + "ERROR_MESSAGE = ? "
            + "WHERE REQUEST_ID = ?" + "AND BUSINESS_STATUS = 'IN_PROGRESS'";

}
