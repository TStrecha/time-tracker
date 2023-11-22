package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.constant.TaskFilterField;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.TaskEntity_;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.util.FilterUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
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
            "AND user_id = :loggedUserId AND active = true LIMIT :resultLimit", nativeQuery = true)
    List<TaskEntity> searchForTasks(String query, Long loggedUserId, Long resultLimit);

    Optional<TaskEntity> findByIdAndUser(Long id, UserEntity user);

    default Page<TaskEntity> findByFilter(TaskFilter taskFilter, Pageable pageable, LoggedUser loggedUser){
        return findAll((Specification<TaskEntity>) (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(TaskEntity_.USER), loggedUser.getUserEntity()));

            var predicatesFromFilter = taskFilter.getFieldFilters().entrySet().stream()
                    .map(entry -> this.getPredicate(entry.getKey(), entry.getValue(), root, cb))
                    .toList();
            predicates.addAll(predicatesFromFilter);

            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    default Predicate getPredicate(TaskFilterField taskFilterField, String value, Root<TaskEntity> root, CriteriaBuilder cb) {
        return switch (taskFilterField) {
            case ID -> cb.equal(root.get(TaskEntity_.ID), value);
            case NAME_SIMPLE ->
                    cb.like(cb.lower(root.get(TaskEntity_.NAME_SIMPLE)), FilterUtils.enrichLikeStatements(value));
            case CUSTOM_ID ->
                    cb.like(root.get(TaskEntity_.CUSTOM_ID).as(String.class), FilterUtils.enrichLikeStatements(value));
            case NOTE -> cb.like(cb.lower(root.get(TaskEntity_.NOTE)), FilterUtils.enrichLikeStatements(value));
            case DESCRIPTION ->
                    cb.like(cb.lower(root.get(TaskEntity_.DESCRIPTION)), FilterUtils.enrichLikeStatements(value));
            case STATUS -> cb.equal(root.get(TaskEntity_.STATUS), TaskStatus.valueOf(value));
            case ESTIMATE -> cb.equal(root.get(TaskEntity_.ESTIMATE), value);
            case ACTIVE -> value.equalsIgnoreCase("true") ?
                    cb.isTrue(root.get(TaskEntity_.ACTIVE)) :
                    cb.isFalse(root.get(TaskEntity_.ACTIVE));
            default -> cb.equal(root.get(TaskEntity_.ID), value);
        };
    }
}