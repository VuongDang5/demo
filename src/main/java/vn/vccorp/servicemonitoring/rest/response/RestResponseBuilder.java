package vn.vccorp.servicemonitoring.rest.response;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
public class RestResponseBuilder {

    public static <T> ResponseEntity<T> buildSuccessObjectResponse(final T target, HttpStatus code) {
        return new ResponseEntity<>(target, code);
    }

    public static <T> ResponseEntity<T> buildSuccessObjectResponse(final T target) {
        return new ResponseEntity<>(target, HttpStatus.OK);
    }

    public static <T> ResponseEntity<List<T>> buildSuccessCollectionResponse(final List<T> collection) {
        return new ResponseEntity<>(collection, HttpStatus.OK);
    }

    public static <T> ResponseEntity<Page<T>> buildSuccessPagingResponse(final Page<T> pagingResponeData) {
        return new ResponseEntity<>(pagingResponeData, HttpStatus.OK);
    }

    public static ResponseEntity<Void> buildEmptyResponse() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
