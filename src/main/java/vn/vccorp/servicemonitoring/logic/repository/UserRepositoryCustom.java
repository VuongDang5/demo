package vn.vccorp.servicemonitoring.logic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.vccorp.servicemonitoring.dto.ListDTO;

import java.util.List;

@Repository
public interface UserRepositoryCustom {
    PageImpl<ListDTO> getAllUser(Pageable page);
}
