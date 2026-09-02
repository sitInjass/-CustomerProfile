/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.bean;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author ِAshraf.M.Fahmawi
 */
public class HttpTextResult {

    private int statusCode;
    private String reasonPhrase;
    private String body;
    private MultivaluedMap<String, Object> headers;

    public HttpTextResult() {
    }

    public HttpTextResult(
            int statusCode,
            String reasonPhrase,
            String body,
            MultivaluedMap<String, Object> headers) {

        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(
            MultivaluedMap<String, Object> headers) {

        this.headers = headers;
    }

    public boolean isSuccessful() {

        return statusCode >= 200
                && statusCode < 300;
    }
}
