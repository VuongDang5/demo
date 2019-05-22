package vn.vccorp.servicemonitoring.rest.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import vn.vccorp.servicemonitoring.exception.ApplicationException;

import java.util.Map;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
@ControllerAdvice
public class RestExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ResponseEntity<BaseResponse> handleHttpClientException(HttpClientErrorException e) {
        LOGGER.error("HttpClientErrorException {}", e);
        Map<String, String> failObject = null;
        String errorMessage = e.getResponseBodyAsString();
        BaseResponse response = new BaseResponse.Builder()
                .setCode(e.getRawStatusCode())
                .setErrorMessage(errorMessage)
                .setFailObject(failObject)
                .build();
        return RestResponseBuilder.buildSuccessObjectResponse(response, e.getStatusCode());
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public ResponseEntity<BaseResponse> handleApplicationException(ApplicationException e) {
        LOGGER.error("ApplicationException {}", e);
        BaseResponse response = new BaseResponse.Builder()
                .setCode(e.getApplicationError().getErrorCode())
                .setErrorMessage(e.getApplicationError().getMessage())
                .build();
        return RestResponseBuilder.buildSuccessObjectResponse(response, e.getApplicationError().getHttpStatus());
    }
}
