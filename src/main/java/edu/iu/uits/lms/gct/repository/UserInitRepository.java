package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.UserInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserInitRepository extends PagingAndSortingRepository<UserInit, Long> {
   UserInit findByLoginIdAndEnv(String loginId, String env);
}
