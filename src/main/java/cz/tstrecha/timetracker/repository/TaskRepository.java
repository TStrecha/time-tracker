package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    @Query(value = "SELECT * FROM task WHERE (name_simple ILIKE :query OR custom_id ILIKE :query" +
            " OR CONCAT(custom_id, CONCAT(' - ', name_simple)) ILIKE :query ) " +
            "AND user_id = :loggedUserId LIMIT :resultLimit", nativeQuery = true)
    List<TaskEntity> searchForTasks(String query, Long loggedUserId, Long resultLimit);

    Optional<TaskEntity> findByIdAndUser(Long id, UserEntity user);
}
