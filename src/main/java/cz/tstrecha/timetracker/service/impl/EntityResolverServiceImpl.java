package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.service.EntityResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntityResolverServiceImpl implements EntityResolverService {

    private final UserRelationshipRepository userRelationshipRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<Long> resolveUserIds(String entityType, Long targetId) {
        var userId = switch (entityType) {
            case "relationship" -> userRelationshipRepository.findUserId(targetId);
            case "settings" -> userSettingsRepository.findUserId(targetId);
            case "task" -> taskRepository.findUserId(targetId);
            default -> null;
        };

        if(userId == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(userId);
    }
}
