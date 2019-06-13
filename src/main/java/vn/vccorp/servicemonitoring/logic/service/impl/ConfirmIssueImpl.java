package vn.vccorp.servicemonitoring.logic.service.impl;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.vccorp.servicemonitoring.entity.IssueTracking;
import vn.vccorp.servicemonitoring.enumtype.IssueType;
import vn.vccorp.servicemonitoring.logic.repository.IssueTrackingRepository;
import vn.vccorp.servicemonitoring.logic.service.ConfirmIssue;

import java.util.Optional;


@Service
public class ConfirmIssueImpl implements ConfirmIssue {

    @Autowired
    private IssueTrackingRepository issueTrackingRepository;

    @Override
    public void issueResolve(){
        Optional<IssueTracking> lastRecovery = issueTrackingRepository.findByIssueType(IssueType.RECOVERY);
        //First confirm
        if (!lastRecovery.isPresent()){
            if(!issueTrackingRepository.findByIssueType(IssueType.ERROR).isPresent()){
                createIssueRecovery(IssueType.RECOVERY);
                return;
            }
            return;
        }
        //Have confirm yet
        if (!issueTrackingRepository.findByIssueTypeAndTrackingTime(IssueType.ERROR, lastRecovery.get().getTrackingTime()).isEmpty()){
            createIssueRecovery(IssueType.RECOVERY);
        }
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