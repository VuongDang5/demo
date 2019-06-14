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
public class SnapshotInfo {
    private Date time;
    private Float ramFree;
    private Float ramUsed;
    private Float cpuFree;
    private Float cpuUsed;
    private Float gpuFree;
    private Float gpuUsed;
    private Float diskFree;
    private Float diskUsed;
}
