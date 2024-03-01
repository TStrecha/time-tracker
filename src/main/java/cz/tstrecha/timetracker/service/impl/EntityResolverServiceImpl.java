package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.service.EntityResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntityResolverServiceImpl implements EntityResolverService {

    private final UserRelationshipRepository userRelationshipRepository;

    @Override
    public List<Long> resolveUserIds(String entityType, Long targetId) {
        var userId = switch (entityType) {
            case "relationship" -> userRelationshipRepository.findUserId(targetId);
            default -> null;
        };

        if(userId == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(userId);
    }
}
