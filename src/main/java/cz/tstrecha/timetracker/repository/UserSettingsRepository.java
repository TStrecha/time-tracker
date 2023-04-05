package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserSettingsRepository extends JpaRepository<UserSettingsEntity, Long> {

    @Query("SELECT us FROM UserSettings us WHERE us.user = :user AND (us.validTo IS NULL OR us.validTo > NOW())")
    List<UserSettingsEntity> findActiveUserSettings(UserEntity user);
}
