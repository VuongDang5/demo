package vn.vccorp.servicemonitoring.enumtype;

import org.springframework.http.HttpStatus;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public enum ApplicationError {

    UNEXPECTED_EXCEPTION(10000, "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_TYPE_IS_NOT_NULL(10001, "User type is not null", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_TYPE(10002, "Invalid User type", HttpStatus.BAD_REQUEST),
    ACCOUNT_EXISTED(10003, "User Existed", HttpStatus.BAD_REQUEST),
    NOT_FOUND(10004, "Not found", HttpStatus.BAD_REQUEST),
    NOT_FOUND_OR_INVALID_ACCOUNT_ID(10005, "Not found or invalid AccountId", HttpStatus.BAD_REQUEST),
    EXISTING_EMAIL(10006, "This email has already been used", HttpStatus.BAD_REQUEST),
    ACCOUNT_EXISTED_MOBILE(10007, "This mobile had already registered", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(10008, "Invalid email or mobile phone", HttpStatus.BAD_REQUEST),
    FILE_IS_EMPTY(10009, "File is empty", HttpStatus.BAD_REQUEST),
    UPLOAD_FAIL(100010, "Upload file failed", HttpStatus.BAD_REQUEST),
    IMG_NOT_VALID(100011, "Image must be jpg format", HttpStatus.BAD_REQUEST),
    MODEL_NOT_VALID(100012, "Model does not exist", HttpStatus.BAD_REQUEST),
    LIST_MODEL_EMPTY(100013, "List model is empty", HttpStatus.BAD_REQUEST),
    MODEL_NOT_SET(100014, "Model has not been set", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(100015, "Token is invalid", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(100017, "Token is expired or invalid", HttpStatus.BAD_REQUEST),
    NOT_FOUND_OR_INVALID_ACCOUNT_SOCIAL_ID(100018, "Not fount or invalid account social id", HttpStatus.BAD_REQUEST),
    NOT_FOUND_OR_INVALID_EMAIL(100019, "Not fount or invalid email", HttpStatus.BAD_REQUEST),
    OVER_TIMES_TO_GET_TOKEN(100020, "Over times to get a token. Please check your email to activate your account or use another email to register again", HttpStatus.BAD_REQUEST),
    NOT_FOUND_ACCOUNT_SOCIAL(100021, "Not fount account social", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_OR_PASSWORD(100022, "Invalid email or password", HttpStatus.BAD_REQUEST),
    NOT_FOUND_OR_INVALID_NOTIFICATION_ID(100023, "Not found or invalid notification id", HttpStatus.BAD_REQUEST),
    INVALID_NOTIFY_STATUS(100024, "Invalid notify status", HttpStatus.BAD_REQUEST);

    private int errorCode;
    private String message;
    private HttpStatus httpStatus;

    ApplicationError(int errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
