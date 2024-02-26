package cz.tstrecha.timetracker.service;

import java.util.List;

public interface EntityResolverService {

    List<Long> resolveUserIds(String entityType, Object targetId);
}
