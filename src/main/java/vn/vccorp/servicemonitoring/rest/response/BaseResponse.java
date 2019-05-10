package vn.vccorp.servicemonitoring.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse {
    private Date timestamp;
    private int code;
    private String errorMessage;
    private Object failObject;
    private Object successObject;

    private BaseResponse(int code, String errorMessage, Object failObject, Object successObject) {
        this.timestamp = new Date();
        this.code = code;
        this.errorMessage = errorMessage;
        this.failObject = failObject;
        this.successObject = successObject;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getFailObject() {
        return failObject;
    }

    public Object getSuccessObject() {
        return successObject;
    }

    public static class Builder {
        private int code = org.springframework.http.HttpStatus.OK.value();
        private String errorMessage;
        private Object failObject;
        private Object successObject;

        public BaseResponse build() {
            return new BaseResponse(code, errorMessage, failObject, successObject);
        }

        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder setFailObject(Object failObject) {
            this.failObject = failObject;
            return this;
        }

        public Builder setSuccessObject(Object successObject) {
            this.successObject = successObject;
            return this;
        }
    }
}
