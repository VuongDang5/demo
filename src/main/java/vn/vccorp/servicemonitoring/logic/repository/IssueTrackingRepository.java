/**
 * Created by: tuyennta
 * Created on: 31/05/2019 15:44
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.entity.Service;
import vn.vccorp.servicemonitoring.enumtype.IssueType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IssueTrackingRepository extends JpaRepository<IssueTracking, Integer> {
    Optional<IssueTracking> findTopByServiceOrderByTrackingTimeDesc(Service service);

    Optional<IssueTracking> findTopByServiceAndIssueTypeOrderByTrackingTimeDesc(Service service,IssueType issueType);

    List<IssueTracking> findAllByServiceOrderByTrackingTimeDesc(Service service);

    List<IssueTracking> findAllByServiceAndTrackingTimeGreaterThanOrderByTrackingTimeDesc(Service service, Date trackingTime);
}
