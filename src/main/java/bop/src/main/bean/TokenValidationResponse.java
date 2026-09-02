/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.bean;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class TokenValidationResponse {

    private int status;
    private String message;
    private boolean valid;
    private String userId;
    private String sessionStatus;
    private String idleExpiresAt;
    private String absoluteExpiresAt;

    public TokenValidationResponse() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getIdleExpiresAt() {
        return idleExpiresAt;
    }

    public void setIdleExpiresAt(String idleExpiresAt) {
        this.idleExpiresAt = idleExpiresAt;
    }

    public String getAbsoluteExpiresAt() {
        return absoluteExpiresAt;
    }

    public void setAbsoluteExpiresAt(String absoluteExpiresAt) {
        this.absoluteExpiresAt = absoluteExpiresAt;
    }
}
