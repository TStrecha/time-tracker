package cz.tstrecha.timetracker.repository;

import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettingsEntity, Long> {

}
