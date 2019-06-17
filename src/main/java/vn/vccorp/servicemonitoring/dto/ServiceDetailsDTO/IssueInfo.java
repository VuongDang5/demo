package vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssueInfo {
    private String detail;
    private String issueType;
    private Date trackingTime;
    private String userAction;
    private String name;
    private String email;
}
