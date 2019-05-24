/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:48
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.ServiceInfoDTO;

public interface ServiceRepositoryCustom {
    Page<ServiceInfoDTO> showAllService(Pageable firstPageWithFourElements);
}
