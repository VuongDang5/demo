/**
 * Created by: tuyennta
 * Created on: 30/05/2019 16:53
 */

package vn.vccorp.servicemonitoring.logic.scheduler;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.enumtype.Status;
import vn.vccorp.servicemonitoring.logic.repository.ConfigurationRepository;
import vn.vccorp.servicemonitoring.logic.repository.IssueTrackingRepository;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
import vn.vccorp.servicemonitoring.logic.service.ConfirmIssue;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;

@Component
public class HealthCheckScheduler implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckScheduler.class);
    @Autowired
    private ConfigurationRepository configurationRepository;
    @Autowired
    private HealthCheckService healthCheckService;
    @Value("${default.config.healthCheckSchedule}")
    private String defaultCron;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ConfirmIssue confirmIssue;
    @Autowired
    private IssueTrackingRepository issueTrackingRepository;
    /**
     * This function is called frequently to check services' health including resources usage, logging status, service status
     */
    public void frequentlyCheck() {
        //health check service
        Page<Service> services;
        do {
            //get all services
            services = serviceRepository.findAll(PageRequest.of(0, 10));

            //loop through all services and check one by one
            for (Service service : services.getContent()) {

                //health check 1: check service status
                if (healthCheckService.checkServiceStatus(service)) {

                    //health check 2: check service log
                    boolean isIssueLog = healthCheckService.checkLogService(service);

                    //health check 3: checking for usage resources
                    boolean isIssueUsage = healthCheckService.checkResourcesUsage(service);

                    //resolve Issue
                    if (!isIssueLog && !isIssueUsage && !(service.getStatus() == Status.ACTIVE)){
                        //create a new record for issue_tracking
                        String detailMessage = String.format("Service is Recovery");
                        healthCheckService.addingIssueTrackingAndSendReport(service, detailMessage, IssueType.RECOVERY, null);
                    }
                }
            }
        } while (services.hasNext());
    }

    private String getCronExpression() {
        String cron = configurationRepository.findHealthCheckScheduleCron();
        //for the first time we deploy our application then cron expression will not available, so we return default value
        return cron == null ? defaultCron : cron;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this::frequentlyCheck, triggerContext -> {
            String cron = getCronExpression();
            CronTrigger trigger = new CronTrigger(cron);
            return trigger.nextExecutionTime(triggerContext);
        });
    }

}
