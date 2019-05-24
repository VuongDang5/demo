/**
 * Created by: tuyennta
 * Created on: 20/05/2019 17:28
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.Service;

public interface ServiceRepository extends ServiceRepositoryCustom, JpaRepository<Service, Integer> {

}
