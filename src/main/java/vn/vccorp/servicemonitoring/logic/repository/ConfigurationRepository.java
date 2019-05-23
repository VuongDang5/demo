package vn.vccorp.servicemonitoring.logic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.Configuration;
import vn.vccorp.servicemonitoring.entity.User;

public interface ConfigurationRepository extends JpaRepository<Configuration, Integer> {
	
}
