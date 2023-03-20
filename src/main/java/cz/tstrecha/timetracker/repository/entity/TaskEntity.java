package cz.tstrecha.timetracker.repository.entity;

import cz.tstrecha.timetracker.constant.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Task")
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"customId", "user_id"}),
        indexes = {
                @Index(name = "task_id_index", columnList = "id"),
                @Index(name = "cid_index", columnList = "customId"),
                @Index(name = "name_index", columnList = "name")
        }
)
public class TaskEntity {

    private static final String SEQUENCE_NAME = "task_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Column(length = 50)
    private String customId;

    private String name;

    private String nameSimple;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @ManyToOne
    private UserEntity user;

    private Long estimate;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    private boolean active = true;
}
