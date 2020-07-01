package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.GctProperty;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface GctPropertyRepository extends PagingAndSortingRepository<GctProperty, Long> {

    GctProperty findByKey(String key);
}
