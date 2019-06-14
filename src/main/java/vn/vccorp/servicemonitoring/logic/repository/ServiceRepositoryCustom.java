/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:48
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.vccorp.servicemonitoring.dto.ServiceDetailsDTO.ServiceDetailsDTO;
import vn.vccorp.servicemonitoring.dto.ServiceInfoDTO;

public interface ServiceRepositoryCustom {
    PageImpl<ServiceInfoDTO> showAllService(Pageable firstPageWithFourElements);

    ServiceDetailsDTO showService(int serviceId);

}
