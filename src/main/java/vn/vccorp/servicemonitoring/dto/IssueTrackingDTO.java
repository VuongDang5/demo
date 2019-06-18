package vn.vccorp.servicemonitoring.dto;

import lombok.*;
import vn.vccorp.servicemonitoring.enumtype.IssueType;

import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueTrackingDTO {
    private  Integer serviceId;

    private Date trackingTime;

    private IssueType issueType;

    @Size(max = 10000)
    private String detail;

    @Size(max = 1000)
    private String userAction;
}
