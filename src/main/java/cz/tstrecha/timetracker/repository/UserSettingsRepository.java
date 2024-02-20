package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSettingsRepository extends JpaRepository<UserSettingsEntity, Long> {

    @Query("SELECT us FROM user_settings us WHERE us.user = :user AND (us.validTo IS NULL OR us.validTo > NOW())")
    List<UserSettingsEntity> findActiveUserSettings(@Param("user") UserEntity user);

    boolean existsByUserAndName(UserEntity user, String name);

    boolean existsByUserIdAndNameAndIdIsNot(Long userId, String name, Long id);
}
