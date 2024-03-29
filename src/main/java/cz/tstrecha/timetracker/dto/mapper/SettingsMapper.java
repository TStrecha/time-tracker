package cz.tstrecha.timetracker.dto.mapper;

import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface SettingsMapper {

    SettingsCreateUpdateDTO toDTO(UserSettingsEntity source);

    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    UserSettingsEntity toEntity(SettingsCreateUpdateDTO source, UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "validFrom", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateSetting(SettingsCreateUpdateDTO source, @MappingTarget UserSettingsEntity target);
}
