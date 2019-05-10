package vn.vccorp.servicemonitoring.rest.api;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.rest.response.BaseResponse;
import vn.vccorp.servicemonitoring.rest.response.RestResponseBuilder;
import vn.vccorp.servicemonitoring.utils.AppConstants;
import vn.vccorp.servicemonitoring.utils.AppUtils;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
@RestController
@RequestMapping(value = AppConstants.API_MAPPING + "/account")
public class AccountResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResources.class);

    @Autowired
    private Messages messages;

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Login account", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> login(@RequestParam String email, @RequestParam String password) {
        LOGGER.info("Receive request to login with email: {}, password: {}", email, password);
        if (!AppUtils.isValidEmailAddress(email)) {
            throw new ApplicationException(ApplicationError.INVALID_EMAIL_OR_PASSWORD);
        }
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(messages.get("login.msg.response"));
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

}
