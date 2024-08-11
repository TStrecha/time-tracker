package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRelationshipRepository extends JpaRepository<UserRelationshipEntity, Long> {

    boolean existsByFromAndTo(UserEntity from, UserEntity to);

    @Query("SELECT ur.from.id FROM user_relationship ur WHERE ur.id = :relationshipId")
    Long findUserId(@Param("relationshipId") Long relationshipId);
}
