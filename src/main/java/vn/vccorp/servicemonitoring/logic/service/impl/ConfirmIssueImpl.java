package vn.vccorp.servicemonitoring.logic.service.impl;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.logic.repository.IssueTrackingRepository;
import vn.vccorp.servicemonitoring.logic.service.ConfirmIssue;

import java.util.List;
import java.util.Optional;


@Service
public class ConfirmIssueImpl implements ConfirmIssue {

    @Autowired
    private IssueTrackingRepository issueTrackingRepository;

    @Override
    public void issueResolve(){
        Optional<IssueTracking> lastRecovery = issueTrackingRepository.findTopByIssueTypeOrderByTrackingTimeDesc(IssueType.ERROR);
        //First confirm
        if (!lastRecovery.isPresent()){
            if(!issueTrackingRepository.findTopByIssueTypeOrderByTrackingTimeDesc(IssueType.ERROR).isPresent()){
                createIssueRecovery(IssueType.RECOVERY);
                return;
            }
            return;
        }

        //
        if (issueTrackingRepository.findTopByOrderByTrackingTimeDesc() == lastRecovery.get()){
            return;
        }

        //Have confirm yet
        List<IssueTracking> test = issueTrackingRepository.findAllByIssueTypeAndTrackingTimeGreaterThanOrderByTrackingTimeDesc(IssueType.ERROR, lastRecovery.get().getTrackingTime());
        if (test.isEmpty()){
            createIssueRecovery(IssueType.RECOVERY);
        }
        return;
    }

    @Override
    public void userConfirmIssue(){

    }

    @Override
    public void disableIssue(){

    }

    private void createIssueRecovery(IssueType issueType){
        //create a new record for issue_tracking
        IssueTracking issueTracking = new IssueTracking();
        issueTracking.setIssueType(issueType);
        issueTracking.setTrackingTime(LocalDateTime.now().toDate());
        issueTrackingRepository.save(issueTracking);
    }
}