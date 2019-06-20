/**
 * Created by: tuyennta
 * Created on: 30/05/2019 15:01
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vccorp.servicemonitoring.entity.Snapshot;

public interface SnapshotRepository extends JpaRepository<Snapshot, Integer> {
}
