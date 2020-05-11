package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.DropboxInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface DropboxInitRepository extends PagingAndSortingRepository<DropboxInit, Long> {

}
