package vn.vccorp.servicemonitoring;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import vn.vccorp.servicemonitoring.dto.ServerDTO;
import vn.vccorp.servicemonitoring.dto.ServiceDTO;
import vn.vccorp.servicemonitoring.dto.ServiceErrorDTO;
import vn.vccorp.servicemonitoring.enumtype.Language;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.logic.service.*;
import vn.vccorp.servicemonitoring.utils.AppUtils;
import vn.vccorp.servicemonitoring.utils.BeanUtils;


import java.util.Arrays;

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
                .name("test1")
                .status(Status.ACTIVE)
                .deployCommand("nohup java -jar target/tdcd-crawler-service.jar > out.log &")
                .deployDir("/home/kibou/Downloads/test/")
                .description("test")
                .language(Language.JAVA.name())
                .logDir("/home/kibou/Downloads/test/test")
                .logFile("test")
                .pid("3863")
                .project("test")
                .serverId(35)
                .serverPort("26346")
                //.maintainerIds(Collections.singletonList(0))
                .ownerId(4)
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

    @Test
    public void testHealthCheck3(){
        BeanUtils.getBean(HealthCheckService.class).checkResourcesUsage(null);
    }

    @Test
    public void testSendServiceErrorReport(){
        ServiceErrorDTO errorDTO = ServiceErrorDTO.builder()
                .serviceName("test")
                .deployedServer("localhost")
                .detail("test")
                .linkOnTool("link")
                .problem("pro")
                .status("active")
                .build();
        EmailService emailService = BeanUtils.getBean(EmailService.class);
        String body = emailService.createBodyEmailFromTemplate(ImmutableMap.of("service", errorDTO), "healthcheck-service-error-template.ftl");
        try {
            emailService.sendEmail(Arrays.asList("anhtuyenpro94@gmail.com"), null, null, "test", body, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegisterServer(){
        ServerDTO serverDTO = ServerDTO.builder()
                .name("testlocal")
                .ip("127.0.0.1")
                .description("test1")
                .rootPath("/home/")
                .build();
        BeanUtils.getBean(MonitorServer.class).registerServer(serverDTO);
    }

    @Test
    public void testGetAllServer(){
        BeanUtils.getBean(MonitorServer.class).getAllServer(PageRequest.of(1, 10));
    }

    @Test
    public void testGetAllUser(){
        BeanUtils.getBean(UserService.class).listAllUser(1, 10);
    }

    @Test
    public void testConfirmIssue() { BeanUtils.getBean(ConfirmIssue.class).userConfirmIssue(9);}

    @Test
    public void testDisableService() { BeanUtils.getBean(ConfirmIssue.class).disableIssue(9, "Mon Jun 17 15:30:52 ICT 2019");}
}
