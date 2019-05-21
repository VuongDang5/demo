package vn.vccorp.servicemonitoring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.logic.service.MonitorService;
import vn.vccorp.servicemonitoring.utils.AppUtils;
import vn.vccorp.servicemonitoring.utils.BeanUtils;

import java.util.Collections;

/**
 * Name: Cường Tuấn Nguyễn.
 * Date: 05/07/2018.
 * Time: 15:00.
 */
@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@RunWith(SpringRunner.class)
public class TestAll {

    @Test
    public void testExecuteTerminalCommand(){
        AppUtils.executeCommand("ssh localhost -t 'ps -p 5079 > /dev/null'; echo $?").forEach(l -> System.out.println(l));
    }

    @Test
    public void testRegisterService(){
        ServiceDTO serviceDTO = ServiceDTO.builder()
                .name("test")
                .status(Status.ACTIVE)
                .deployCommand("test")
                .deployDir("test")
                .description("test")
                .language("test")
                .logDir("test")
                .logFile("test")
                .PID("5079")
                .project("test")
                .serverId("localhost")
                .maintainerIds(Collections.singletonList(1))
                .build();
        BeanUtils.getBean(MonitorService.class).registerService(serviceDTO);
    }

    @Test
    public void testGetStartedDateOfProcess(){
        System.out.println(AppUtils.getStartedDateOfProcess("localhost", "22", "5079"));
    }
}
