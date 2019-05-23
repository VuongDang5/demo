package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.Configuration;

public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {

}
