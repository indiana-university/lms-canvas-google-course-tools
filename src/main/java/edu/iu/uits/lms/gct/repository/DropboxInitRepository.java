package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.DropboxInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DropboxInitRepository extends PagingAndSortingRepository<DropboxInit, Long> {
   List<DropboxInit> findByCourseIdAndEnv(String courseId, String env);
   List<DropboxInit> findByLoginIdAndEnv(String loginIdId, String env);
   DropboxInit findByCourseIdAndLoginIdAndEnv(String courseId, String loginId, String env);
   DropboxInit findByCourseIdAndGoogleLoginIdAndEnv(String courseId, String googleLoginId, String env);
}
