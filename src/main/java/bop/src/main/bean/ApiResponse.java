/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.bean;

/**
 *
 * @author Ashraf.M.Fahmawi
 */
public class ApiResponse {

    private int status;
    private String message;
    private Object data;
    private String requestId;
    private String timestamp;

    public ApiResponse() {
    }

    public ApiResponse(
            int status,
            String message,
            Object data,
            String requestId,
            String timestamp) {

        this.status = status;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = timestamp;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return status == 0;
    }
}
