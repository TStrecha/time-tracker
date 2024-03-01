package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;

import java.util.List;

public interface RelationshipService {

    /**
     * @param userContext
     * @return
     */
    List<ContextUserDTO> getActiveContexts(UserContext userContext);

    /**
     * @param relationshipCreateUpdateRequestDTO
     * @param userContext
     * @return
     */
    RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, UserContext userContext);

    /**
     * @param relationshipCreateUpdateRequestDTO
     * @param userContext
     * @return
     */
    RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, UserContext userContext);

    /**
     * @param id
     * @param userContext
     * @return
     */
    LoginResponseDTO changeContext(Long id, UserContext userContext);
}
