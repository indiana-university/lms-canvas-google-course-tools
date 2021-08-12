package edu.iu.uits.lms.gct.repository;

import edu.iu.uits.lms.gct.model.GroupsInit;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface GroupsInitRepository extends PagingAndSortingRepository<GroupsInit, Long> {

   List<GroupsInit> findByCanvasCourseIdAndEnv(String canvasCourseId, String env);
   GroupsInit findByCanvasCourseIdAndCanvasGroupIdAndEnv(String canvasCourseId, String canvasGroupId, String env);
   GroupsInit findByCanvasCourseIdAndFolderIdAndEnv(String canvasCourseId, String folderId, String env);

}
