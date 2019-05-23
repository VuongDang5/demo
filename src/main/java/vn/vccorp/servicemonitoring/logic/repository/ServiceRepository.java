/**
 * Created by: tuyennta
 * Created on: 20/05/2019 17:28
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.Service;

import java.awt.print.Pageable;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findAll();

}
