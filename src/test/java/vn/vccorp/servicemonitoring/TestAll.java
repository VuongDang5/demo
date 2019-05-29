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
 * Name:
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
                .deployCommand("nohup java -jar target/tdcd-crawler-service.jar > out.log &")
                .deployDir("/home/tuyennta/projects/freelance/blogcrawler")
                .description("test")
                .language("java")
                .logDir("/home/tuyennta/projects/freelance/blogcrawler/")
                .logFile("out.log")
                .pid("14516")
                .project("test")
                .serverId(1)
                .serverIp("localhost")
                .maintainerIds(Collections.singletonList(2))
                .ownerId(1)
                .build();
        BeanUtils.getBean(MonitorService.class).registerService(serviceDTO);
    }

    @Test
    public void testGetStartedDateOfProcess(){
        System.out.println(AppUtils.getStartedDateOfProcess("localhost", "tuyennta", "22", "5079"));
    }

    @Test
    public void testStartService(){
        BeanUtils.getBean(MonitorService.class).startService(3);
    }

    @Test
    public void testStopService(){
        BeanUtils.getBean(MonitorService.class).stopService(3);
    }

}
