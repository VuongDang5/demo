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

    private long checkedLine;
}
