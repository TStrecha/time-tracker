package cz.tstrecha.timetracker.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UserRelationship")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"from_id","to_id"}))
public class UserRelationshipEntity {

    private static final String SEQUENCE_NAME = "user_relationship_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    // to has authority to login as from and edit from's data depending on the permissions of this relationship
    @ManyToOne
    private UserEntity from;
    @ManyToOne
    private UserEntity to;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> permissions;

    private OffsetDateTime activeFrom = OffsetDateTime.now();
    private OffsetDateTime activeTo;

    private boolean secureValues;

}
