package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.DropboxInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DropboxInitRepository extends PagingAndSortingRepository<DropboxInit, Long> {
   List<DropboxInit> findByCourseId(String courseId);
   List<DropboxInit> findByLoginId(String loginIdId);
   DropboxInit findByCourseIdAndLoginId(String courseId, String loginId);
}
