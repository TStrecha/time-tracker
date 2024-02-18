package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.service.EntityResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntityResolverServiceImpl implements EntityResolverService {

    @Override
    public List<Long> resolveUserIds(String entityType, Object targetId) {
        // TODO(TS, 2024/02/18): Implement me.
        return Collections.emptyList();
    }
}
