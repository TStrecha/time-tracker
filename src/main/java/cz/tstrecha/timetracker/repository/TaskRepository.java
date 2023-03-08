package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
