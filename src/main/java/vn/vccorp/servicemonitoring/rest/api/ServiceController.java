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
import vn.vccorp.servicemonitoring.dto.LogServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.dto.UserServiceDTO;
import vn.vccorp.servicemonitoring.enumtype.Role;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.rest.response.BaseResponse;
import vn.vccorp.servicemonitoring.rest.response.RestResponseBuilder;
import vn.vccorp.servicemonitoring.security.AdminAuthorize;
import vn.vccorp.servicemonitoring.security.CurrentUser;
import vn.vccorp.servicemonitoring.security.MaintainerAuthorize;
import vn.vccorp.servicemonitoring.security.OwnerAuthorize;
import vn.vccorp.servicemonitoring.security.UserAuthorize;
import vn.vccorp.servicemonitoring.security.UserPrincipal;
import vn.vccorp.servicemonitoring.utils.AppConstants;

import java.io.IOException;

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
    
    @RequestMapping(value = "/delete-log", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Delete Log", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> deleteLog(@CurrentUser UserPrincipal currentUser, @RequestBody int id) {
        LOGGER.info("Receive request of user: {}, mail: {}, role: {}", currentUser.getName(), currentUser.getEmail(), currentUser.getAuthorities());
        monitorService.deleteLog(id);
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
        BaseResponse.Builder builder = new BaseResponse.Builder();
        try {
            monitorService.deployService(serviceDTO, currentUser.getId(), jar, originalJar, dependencies, modelFile, sourceCode, dockerFile);
            builder.setSuccessObject(true);
        } catch (IOException e) {
            LOGGER.error("Exception while deploy service", e);
            builder.setFailObject(e);
            builder.setErrorMessage(messages.get("service.deploy.fail"));
        }
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/get-log-service", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get 1000 log service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> getLogService(@RequestBody LogServiceDTO logServiceDTO) {
        LOGGER.info("Get log a service with id: " + logServiceDTO.getServiceId());

        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(monitorService.getLogService(logServiceDTO));
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/show-all-service/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Show all service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @UserAuthorize
    public ResponseEntity<Object> showAllService(@CurrentUser UserPrincipal currentUser,
                                                 @RequestParam int currentPage, @RequestParam int pageSize) {
        monitorService.showAllService(currentPage, pageSize);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(monitorService.showAllService(currentPage, pageSize));
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
    
    @RequestMapping(value = "/add-service-manager", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "add service magager", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @OwnerAuthorize(serviceId = "#serviceId")
    public ResponseEntity<Object> addServiceOwner(@CurrentUser UserPrincipal currentUser, @RequestBody UserServiceDTO userServiceDTO) {
        monitorService.addServiceOwner(userServiceDTO.getUserId(), userServiceDTO.getServiceId(), Role.valueOf(userServiceDTO.getRole()));
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/edit-service/{serviceId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Edit Information Service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @MaintainerAuthorize(serviceId = "#serviceId")
    public ResponseEntity<Object> editService(@CurrentUser UserPrincipal currentUser, @PathVariable int serviceId, @RequestBody ServiceDTO serviceDTO) {
        LOGGER.info("Receive request to edit a service with id: " + serviceId);
        monitorService.editService(serviceId, serviceDTO);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        //builder.setSuccessObject(true);
        try {
            monitorService.editService(serviceId, serviceDTO);
            builder.setSuccessObject(true);
        } catch (Exception e) {
            LOGGER.error("Exception while edit service", e);
            builder.setFailObject(e);
            builder.setErrorMessage(messages.get("service.edit.fail"));
        }
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }
}
