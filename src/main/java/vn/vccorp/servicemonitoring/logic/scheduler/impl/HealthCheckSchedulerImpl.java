/**
 * Created by: tuyennta
 * Created on: 30/05/2019 16:53
 */

package vn.vccorp.servicemonitoring.logic.scheduler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.vccorp.servicemonitoring.logic.repository.ConfigurationRepository;
import vn.vccorp.servicemonitoring.logic.scheduler.HealthCheckScheduler;
import vn.vccorp.servicemonitoring.logic.service.HealthCheckService;

@Component
public class HealthCheckSchedulerImpl implements HealthCheckScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckSchedulerImpl.class);
    @Autowired
    private ConfigurationRepository configurationRepository;
    @Autowired
    private HealthCheckService healthCheckService;
    @Value("${default.config.healthCheckSchedule}")
    private String defaultCron;

    @Scheduled(cron = "#{@getCronExpression}")
    @Override
    public void frequentlyCheck() {
        LOGGER.info("check");
        healthCheckService.checkResources();
        LOGGER.info("done");
    }

    @Bean
    public String getCronExpression() {
        String cron = configurationRepository.findHealthCheckScheduleCron();
        //for the first time we deploy our application then cron expression will not available, so we return default value
        return cron == null ? defaultCron : cron;
    }
}
