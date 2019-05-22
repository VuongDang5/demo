package vn.vccorp.servicemonitoring.exception;

import vn.vccorp.servicemonitoring.enumtype.ApplicationError;

import java.util.function.Supplier;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class ApplicationException extends RuntimeException {

    private ApplicationError applicationError;

    public ApplicationException(ApplicationError applicationError) {
        super(applicationError.getMessage());
        this.applicationError = applicationError;
    }

    public ApplicationException(Throwable e) {
        super(e);
    }

    public ApplicationException(String s) {
        super(s);
    }

    public ApplicationError getApplicationError() {
        return applicationError;
    }

    public void setApplicationError(ApplicationError applicationError) {
        this.applicationError = applicationError;
    }

}
