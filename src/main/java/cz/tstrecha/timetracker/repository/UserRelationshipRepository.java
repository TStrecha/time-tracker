package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRelationshipRepository extends JpaRepository<UserRelationshipEntity, Long> {

    boolean existsByFromAndTo(UserEntity from, UserEntity to);

    Optional<UserRelationshipEntity> findByFromAndTo(UserEntity from, UserEntity to);
}
