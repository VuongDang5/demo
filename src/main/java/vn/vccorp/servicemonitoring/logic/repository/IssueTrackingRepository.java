/**
 * Created by: tuyennta
 * Created on: 31/05/2019 15:44
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.IssueTracking;

public interface IssueTrackingRepository extends JpaRepository<IssueTracking, Integer> {
}
