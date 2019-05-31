/**
 * Created by: tuyennta
 * Created on: 30/05/2019 16:53
 */

package vn.vccorp.servicemonitoring.logic.scheduler;

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
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.logic.repository.ConfigurationRepository;
import vn.vccorp.servicemonitoring.logic.repository.ServiceRepository;
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


    private void frequentlyCheck() {
        Page<Service> services;
        do {
            //get all services
            services = serviceRepository.findAll(PageRequest.of(0, 10));

            //loop through all services and check one by one
            for (Service service : services.getContent()) {

                //TODO add health check 1 here
                healthCheckService.healthCheck1(service);

                //TODO add health check 2 here
                healthCheckService.checkLogService(service);

                //health check 3: checking for usage resources
                healthCheckService.checkResources(service);

            }
        } while (services.hasNext());
    }

    public String getCronExpression() {
        String cron = configurationRepository.findHealthCheckScheduleCron();
        //for the first time we deploy our application then cron expression will not available, so we return default value
        return cron == null ? defaultCron : cron;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this::frequentlyCheck, triggerContext -> {
            String cron = getCronExpression();
            LOGGER.info(cron);
            CronTrigger trigger = new CronTrigger(cron);
            return trigger.nextExecutionTime(triggerContext);
        });
    }

}
