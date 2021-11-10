package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.CourseInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CourseInitRepository extends PagingAndSortingRepository<CourseInit, Long> {

   CourseInit findByCourseIdAndEnv(String courseId, String env);
   CourseInit findBySisCourseIdAndEnv(String sisCourseId, String env);
   List<CourseInit> findBySyncStatusAndEnvOrderByCourseId(CourseInit.SYNC_STATUS syncStatus, String env);

}
