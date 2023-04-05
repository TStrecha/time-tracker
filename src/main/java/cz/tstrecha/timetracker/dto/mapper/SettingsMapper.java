package cz.tstrecha.timetracker.dto.mapper;

import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserSettingsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SettingsMapper {

    SettingsCreateUpdateDTO toDTO(UserSettingsEntity source);

    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    UserSettingsEntity toEntity(SettingsCreateUpdateDTO source, UserEntity user);
}
