package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.vccorp.servicemonitoring.dto.UserInfoDTO;

@Repository
public interface UserRepositoryCustom {
    PageImpl<UserInfoDTO> getAllUser(Pageable page);
}
