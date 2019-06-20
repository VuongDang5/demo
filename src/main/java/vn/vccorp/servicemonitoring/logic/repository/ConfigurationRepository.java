package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.Configuration;

public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

    @Query("select c.healthCheckSchedule from Configuration c")
    String findHealthCheckScheduleCron();
    
    @Query("select c.reportSchedule from Configuration c")
    String findReportScheduleCron();
}
