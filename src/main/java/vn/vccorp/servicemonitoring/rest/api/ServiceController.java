/**
 * Created by: tuyennta
 * Created on: 20/05/2019 09:59
 */

package vn.vccorp.servicemonitoring.rest.api;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.rest.response.BaseResponse;
import vn.vccorp.servicemonitoring.rest.response.RestResponseBuilder;
import vn.vccorp.servicemonitoring.security.CurrentUser;
import vn.vccorp.servicemonitoring.security.UserAuthorize;
import vn.vccorp.servicemonitoring.security.UserPrincipal;
import vn.vccorp.servicemonitoring.utils.AppConstants;

import java.util.List;

@RequestMapping(value = AppConstants.API_MAPPING + "/service")
@RestController
public class ServiceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private Messages messages;
    @Autowired
    private MonitorService monitorService;

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Register old service to monitor on this system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> registerOldService(@RequestBody ServiceDTO serviceDTO){
        LOGGER.info("Receive request to register old service to monitor on this system");
        monitorService.registerService(serviceDTO);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/show-all-service/{pageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Show all service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @UserAuthorize
    public ResponseEntity<Object> showAllService(@CurrentUser UserPrincipal currentUser, @PathVariable int pageId) {
        monitorService.showAllService(pageId);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(monitorService.showAllService(pageId));
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/show-service/{serviceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Show service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @UserAuthorize
    public ResponseEntity<Object> showService(@CurrentUser UserPrincipal currentUser, @PathVariable int serviceId) {
        monitorService.showService(serviceId);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(monitorService.showService(serviceId));
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }
}
