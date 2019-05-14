package vn.vccorp.servicemonitoring.rest.api;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.vccorp.servicemonitoring.dto.UserDTO;
import vn.vccorp.servicemonitoring.enumtype.ApplicationError;
import vn.vccorp.servicemonitoring.exception.ApplicationException;
import vn.vccorp.servicemonitoring.logic.service.AccountService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.rest.response.BaseResponse;
import vn.vccorp.servicemonitoring.rest.response.RestResponseBuilder;
import vn.vccorp.servicemonitoring.security.*;
import vn.vccorp.servicemonitoring.utils.AppConstants;
import vn.vccorp.servicemonitoring.utils.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    AccountService accountService;

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Login account", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> login(@RequestParam String email, @RequestParam String password, HttpServletRequest req, HttpServletResponse res) {
        LOGGER.info("Receive request to login with email: {}, password: {}", email, password);
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
            throw new ApplicationException(ApplicationError.INVALID_EMAIL_OR_PASSWORD);
        }
        BaseResponse.Builder builder = new BaseResponse.Builder();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = BeanUtils.getBean(JwtTokenProvider.class).generateToken(authentication);
            RequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest savedRequest = requestCache.getRequest(req, res);
            if (authentication != null) {
                builder.setSuccessObject(ImmutableMap.of("token", token, "success", true, "prevUrl", savedRequest == null ? "" : savedRequest.getRedirectUrl()));
                builder.setCode(HttpStatus.OK.value());
            } else {
                throw new ApplicationException(ApplicationError.UNEXPECTED_EXCEPTION);
            }

        } catch (Exception e) {
            LOGGER.info("Login fail");
            builder.setCode(HttpStatus.UNAUTHORIZED.value());
            builder.setErrorMessage(e.getMessage());
            builder.setFailObject(ImmutableMap.of("success", false));
        }
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Add new account", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @AdminAuthorize
    public ResponseEntity<Object> addAccount(@RequestBody @Valid UserDTO newUser) {
        LOGGER.info("Receive request to add new user: {}, mail: {}, role: {}", newUser.getName(), newUser.getEmail(), newUser.getRole());
        accountService.addAccount(newUser);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/test1", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Login account", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @UserAuthorize
    public ResponseEntity<Object> test1(@CurrentUser UserPrincipal currentUser) {
        LOGGER.info("Receive request of user: {}, mail: {}, role: {}", currentUser.getName(), currentUser.getEmail(), currentUser.getAuthorities());
        BaseResponse.Builder builder = new BaseResponse.Builder();
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }
}
