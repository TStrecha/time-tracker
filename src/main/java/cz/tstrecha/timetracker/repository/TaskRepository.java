package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.constant.TaskField;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskFilter;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.TaskEntity_;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {

    @Query(value = "SELECT * FROM task WHERE (name_simple ILIKE :query OR custom_id ILIKE :query" +
            " OR CONCAT(custom_id, CONCAT(' - ', name_simple)) ILIKE :query ) " +
            "AND user_id = :loggedUserId LIMIT :resultLimit", nativeQuery = true)
    List<TaskEntity> searchForTasks(String query, Long loggedUserId, Long resultLimit);

    Optional<TaskEntity> findByIdAndUser(Long id, UserEntity user);

    default List<TaskEntity> findByFilter(TaskFilter taskFilter, Pageable pageable, LoggedUser loggedUser){
        return findAll((Specification<TaskEntity>) (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(TaskEntity_.USER), loggedUser.getUserEntity()));

            for (TaskField taskField : taskFilter.getFieldFilters().keySet()) {
                switch (taskField) {
                    case ID -> predicates.add(cb.equal(root.get(TaskEntity_.ID), taskFilter.getFieldFilters().get(taskField)));
                    case NAME_SIMPLE -> predicates.add(cb.like(cb.lower(root.get(TaskEntity_.NAME_SIMPLE)), FilterUtils.enrichLikeStatements(taskFilter.getFieldFilters().get(taskField))));
                    case CUSTOM_ID -> predicates.add(cb.like(root.get(TaskEntity_.CUSTOM_ID).as(String.class), FilterUtils.enrichLikeStatements(taskFilter.getFieldFilters().get(taskField))));
                    case NOTE -> predicates.add(cb.like(cb.lower(root.get(TaskEntity_.NOTE)), FilterUtils.enrichLikeStatements(taskFilter.getFieldFilters().get(taskField))));
                    case DESCRIPTION -> predicates.add(cb.like(cb.lower(root.get(TaskEntity_.DESCRIPTION)), FilterUtils.enrichLikeStatements(taskFilter.getFieldFilters().get(taskField))));
                    case STATUS -> predicates.add(cb.equal(root.get(TaskEntity_.STATUS), TaskStatus.valueOf(taskFilter.getFieldFilters().get(taskField))));
                    case ESTIMATE -> predicates.add(cb.equal(root.get(TaskEntity_.ESTIMATE), taskFilter.getFieldFilters().get(taskField)));
                    case ACTIVE -> {
                        predicates.add(taskFilter.getFieldFilters().get(taskField).equals("true") ?
                        cb.isTrue(root.get(TaskEntity_.ACTIVE)) :
                        cb.isFalse(root.get(TaskEntity_.ACTIVE)));
                    }
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable).toList();
    }
}
//                    case CUSTOM_ID -> predicates.add(cb.like(root.get(TaskEntity_.CUSTOM_ID), cb.literal(String.valueOf(taskFilter.getFieldFilters().get(taskField)))));
