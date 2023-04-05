package cz.tstrecha.timetracker.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UserSettings")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
})
public class UserSettingsEntity {

    private static final String SEQUENCE_NAME = "user_settings_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime modifiedAt;
    
    @Digits(integer = 50, fraction = 2)
    private BigDecimal moneyPerHour;

    @Digits(integer = 50, fraction = 2)
    private BigDecimal moneyPerMonth;

    @ManyToOne
    private UserEntity user;

    private LocalDate validFrom = LocalDate.now();
    private LocalDate validTo = null;
}