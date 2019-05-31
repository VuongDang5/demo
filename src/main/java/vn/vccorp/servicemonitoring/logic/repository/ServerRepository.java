/**
 * Created by: tuyennta
 * Created on: 24/05/2019 13:43
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.Server;

public interface ServerRepository extends JpaRepository<Server, Integer> {
}
