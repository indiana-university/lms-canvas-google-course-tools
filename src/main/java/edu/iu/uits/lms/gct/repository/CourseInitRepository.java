package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.CourseInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface CourseInitRepository extends PagingAndSortingRepository<CourseInit, Long> {

   CourseInit findByCourseId(String courseId);
   CourseInit findBySisCourseId(String sisCourseId);

}
