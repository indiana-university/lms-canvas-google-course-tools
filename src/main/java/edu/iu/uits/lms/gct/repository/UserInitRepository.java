package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.UserInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserInitRepository extends PagingAndSortingRepository<UserInit, Long> {
   List<UserInit> findByLoginId(String loginIdId);
}
