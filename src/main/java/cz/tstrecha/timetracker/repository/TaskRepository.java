package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    @Query(value = "select * from task where (name_simple ilike :query or custom_id ilike :query" +
            " or concat(custom_id, concat(' - ', name_simple)) ilike :query ) " +
            "and user_id = :loggedUserId limit :limitFromQuery", nativeQuery = true)
    List<TaskEntity> searchIntasks(Long limitFromQuery, String query, long loggedUserId);
}
