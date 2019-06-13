/**
 * Created by: tuyennta
 * Created on: 31/05/2019 15:44
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.enumtype.IssueType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IssueTrackingRepository extends JpaRepository<IssueTracking, Integer> {
    Optional<IssueTracking> findTopByIssueTypeOrderByTrackingTimeDesc(IssueType issueType);

    IssueTracking findTopByOrderByTrackingTimeDesc();

    List<IssueTracking> findAllByIssueTypeAndTrackingTimeGreaterThanOrderByTrackingTimeDesc(IssueType issueType, Date trackingTime);
}
