package vn.vccorp.servicemonitoring.exception;

public class NotLoginException extends RuntimeException {

    public NotLoginException(String message) {
        super(message);
    }

    public NotLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
