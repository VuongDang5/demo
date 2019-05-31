/**
 * Created by: tuyennta
 * Created on: 24/05/2019 18:48
 */

package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import vn.vccorp.servicemonitoring.entity.Service;

import java.util.List;

public interface ServiceRepositoryCustom {
    PageImpl<Service> showAllService(Pageable firstPageWithFourElements);

    Service showService(int serviceId);
}
