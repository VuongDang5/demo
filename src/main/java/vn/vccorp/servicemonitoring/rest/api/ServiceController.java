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
import org.springframework.web.multipart.MultipartFile;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.rest.response.BaseResponse;
import vn.vccorp.servicemonitoring.rest.response.RestResponseBuilder;
import vn.vccorp.servicemonitoring.security.CurrentUser;
import vn.vccorp.servicemonitoring.security.MaintainerAuthorize;
import vn.vccorp.servicemonitoring.security.UserPrincipal;
import vn.vccorp.servicemonitoring.utils.AppConstants;

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
    public ResponseEntity<Object> registerOldService(@RequestBody ServiceDTO serviceDTO) {
        LOGGER.info("Receive request to register old service to monitor on this system");
        monitorService.registerService(serviceDTO);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Start a service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @MaintainerAuthorize(serviceId = "#serviceId")
    public ResponseEntity<Object> startService(@CurrentUser UserPrincipal currentUser, @RequestBody int serviceId) {
        LOGGER.info("Receive request to start a service with id: " + serviceId);
        monitorService.startService(serviceId);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Stop a service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @MaintainerAuthorize(serviceId = "#serviceId")
    public ResponseEntity<Object> stopService(@RequestBody int serviceId) {
        LOGGER.info("Receive request to stop a service with id: " + serviceId);
        monitorService.stopService(serviceId);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/deploy", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE
            , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Deploy new service or re-deploy old service to monitor on this system", produces = MediaType.APPLICATION_JSON_UTF8_VALUE
            , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> deployService(@CurrentUser UserPrincipal currentUser
            , @RequestPart ServiceDTO serviceDTO
            , @RequestPart(value = "jar", required = false) MultipartFile jar
            , @RequestPart(value = "original", required = false) MultipartFile originalJar
            , @RequestPart(value = "dependencies", required = false) MultipartFile dependencies
            , @RequestPart(value = "modelFiles", required = false) MultipartFile modelFile
            , @RequestPart(value = "sourceCode", required = false) MultipartFile sourceCode
            , @RequestPart(value = "dockerFile", required = false) MultipartFile dockerFile) {
        LOGGER.info("Receive request to deploy new service or re-deploy old service to monitor on this system");
        monitorService.deployService(serviceDTO, currentUser.getId(), jar, originalJar, dependencies, modelFile, sourceCode, dockerFile);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }
}
