package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.CourseInit;
import edu.iu.uits.lms.gct.repository.CourseInitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/courseinit")
@Slf4j
public class CourseInitRestController {

   @Autowired
   private CourseInitRepository courseInitRepository = null;

   @GetMapping("/{id}")
   public CourseInit get(@PathVariable Long id) {
      return courseInitRepository.findById(id).orElse(null);
   }

   @GetMapping("/course/{env}/{courseId}")
   public CourseInit getByCourseId(@PathVariable String env, @PathVariable String courseId) {
      return courseInitRepository.findByCourseIdAndEnv(courseId, env);
   }

   @GetMapping("/siscourse/{env}/{sisCourseId}")
   public CourseInit getBySisCourseId(@PathVariable String env, @PathVariable String sisCourseId) {
      return courseInitRepository.findBySisCourseIdAndEnv(sisCourseId, env);
   }

   @PutMapping("/{id}")
   public CourseInit update(@PathVariable Long id, @RequestBody RestCourseInit courseInit) {
      CourseInit updatedCourseInit = courseInitRepository.findById(id).orElse(null);

      if (updatedCourseInit != null) {
         if (courseInit.getCourseId() != null) {
            updatedCourseInit.setCourseId(courseInit.getCourseId());
         }
         if (courseInit.getCoursefilesFolderId() != null) {
            updatedCourseInit.setCoursefilesFolderId(courseInit.getCoursefilesFolderId());
         }
         if (courseInit.getCourseFolderId() != null) {
            updatedCourseInit.setCourseFolderId(courseInit.getCourseFolderId());
         }
         if (courseInit.getDeTeacher() != null) {
            updatedCourseInit.setDeTeacher(courseInit.getDeTeacher());
         }
         if (courseInit.getDropboxFolderId() != null) {
            updatedCourseInit.setDropboxFolderId(courseInit.getDropboxFolderId());
         }
         if (courseInit.getFileRepoId() != null) {
            updatedCourseInit.setFileRepoId(courseInit.getFileRepoId());
         }
         if (courseInit.getInstructorFolderId() != null) {
            updatedCourseInit.setInstructorFolderId(courseInit.getInstructorFolderId());
         }
         if (courseInit.getGroupsFolderId() != null) {
            updatedCourseInit.setGroupsFolderId(courseInit.getGroupsFolderId());
         }
         if (courseInit.getMailingListAddress() != null) {
            updatedCourseInit.setMailingListAddress(courseInit.getMailingListAddress());
         }
         if (courseInit.getSisCourseId() != null) {
            updatedCourseInit.setSisCourseId(courseInit.getSisCourseId());
         }
         if (courseInit.getCourseCode() != null) {
            updatedCourseInit.setCourseCode(courseInit.getCourseCode());
         }
         if (courseInit.getTaTeacher() != null) {
            updatedCourseInit.setTaTeacher(courseInit.getTaTeacher());
         }
         if (courseInit.getEnv() != null) {
            updatedCourseInit.setEnv(courseInit.getEnv());
         }

         return courseInitRepository.save(updatedCourseInit);
      }
      return null;
   }

   @PostMapping("/")
   public CourseInit create(@RequestBody CourseInit courseInit) {
      CourseInit newCourseInit = CourseInit.builder()
            .courseId(courseInit.getCourseId())
            .coursefilesFolderId(courseInit.getCoursefilesFolderId())
            .courseFolderId(courseInit.getCourseFolderId())
            .deTeacher(courseInit.isDeTeacher())
            .dropboxFolderId(courseInit.getDropboxFolderId())
            .fileRepoId(courseInit.getFileRepoId())
            .instructorFolderId(courseInit.getInstructorFolderId())
            .groupsFolderId(courseInit.getGroupsFolderId())
            .mailingListAddress(courseInit.getMailingListAddress())
            .sisCourseId(courseInit.getSisCourseId())
            .courseCode(courseInit.getCourseCode())
            .taTeacher(courseInit.isTaTeacher())
            .env(courseInit.getEnv())
            .build();
      return courseInitRepository.save(newCourseInit);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      courseInitRepository.deleteById(id);
      return "Delete success.";
   }

}
