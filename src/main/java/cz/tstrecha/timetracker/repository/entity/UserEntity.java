package cz.tstrecha.timetracker.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.SecretMode;
import cz.tstrecha.timetracker.constant.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "app_user")
@Table(indexes = @Index(name = "user_id_index", columnList = "id"))
public class UserEntity implements UserDetails {

    private static final String SEQUENCE_NAME = "user_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private SecretMode secretMode = SecretMode.NONE;

    private String firstName;
    private String lastName;

    private String companyName;

    @Email
    @Column(unique = true)
    private String email; // Also user's login
    private String passwordHashed;

    @OneToMany(mappedBy = "user")
    private List<TaskEntity> tasks;

    @OneToMany(mappedBy = "from")
    private List<UserRelationshipEntity> userRelationshipGiving = new ArrayList<>();

    @OneToMany(mappedBy = "to")
    private List<UserRelationshipEntity> userRelationshipReceiving = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserSettingsEntity> settings = new ArrayList<>();

    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime modifiedAt;

    @JsonIgnore
    public String getDisplayName() {
        return getAccountType() == AccountType.PERSON ? STR."\{getFirstName()} \{getLastName()}" : getCompanyName();
    }

    @JsonIgnore
    public List<UserRelationshipEntity> getActiveRelationshipsReceiving() {
        return userRelationshipReceiving.stream()
                .filter(relation ->
                        relation.getActiveFrom().isBefore(OffsetDateTime.now()) &&
                                (relation.getActiveTo() == null || relation.getActiveTo().isAfter(OffsetDateTime.now())))
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(getRole().name()));
    }

    @Override
    public String getPassword() {
        return getPasswordHashed();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}