package vn.vccorp.servicemonitoring.entity;

import lombok.*;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "LogService")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int serviceId;

    private int errorCount;

    private LocalDateTime updatedDate;

    private LocalDateTime createdDate;

    private LocalDateTime lastLoggingDate;

    private long checkedLine;

    @Column(length = Integer.MAX_VALUE)
    private String errorMsg;

    private String logDir;
    public String getLogDir() {
        return logDir.endsWith("/") ? logDir : logDir + "/";
    }

    private String logFile;

    private int consecutiveErrCount;

    private int errorPerHourCount;

    private LocalDateTime hourlyCheck;

    private float logSize;
}
