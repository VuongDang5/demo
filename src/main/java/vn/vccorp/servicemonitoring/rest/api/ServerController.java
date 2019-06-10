package vn.vccorp.servicemonitoring.rest.api;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vccorp.servicemonitoring.dto.ServerDTO;
import vn.vccorp.servicemonitoring.logic.service.MonitorServer;
import vn.vccorp.servicemonitoring.message.Messages;
import vn.vccorp.servicemonitoring.rest.response.BaseResponse;
import vn.vccorp.servicemonitoring.rest.response.RestResponseBuilder;
import vn.vccorp.servicemonitoring.utils.AppConstants;

import javax.validation.Valid;

@RequestMapping(value = AppConstants.API_MAPPING + "/server")
@RestController
public class ServerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private Messages messages;
    @Autowired
    private MonitorServer monitorServer;

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Register new server to monitor", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> registerServer(@RequestBody @Valid ServerDTO serverDTO) {
        LOGGER.info("Receive request to register server");
        monitorServer.registerServer(serverDTO);
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(true);
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }

    @RequestMapping(value = "/all-server", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "show all server and information", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> getAllServer(@RequestParam int currentPage, @RequestParam int pageSize) {
        LOGGER.info("Receive request of all server");
        BaseResponse.Builder builder = new BaseResponse.Builder();
        builder.setSuccessObject(monitorServer.getAllServer(PageRequest.of(currentPage, pageSize)));
        return RestResponseBuilder.buildSuccessObjectResponse(builder.build());
    }
}

