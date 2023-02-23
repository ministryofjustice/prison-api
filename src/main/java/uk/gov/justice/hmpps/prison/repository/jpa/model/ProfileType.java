package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROFILE_TYPES")
@EqualsAndHashCode(of = { "type"}, callSuper = false)
@ToString(of = { "type", "category", "description"})
public class ProfileType extends AuditableEntity {

    @Id
    @Column(name = "PROFILE_TYPE", nullable = false)
    private String type;

    @Column(name = "PROFILE_CATEGORY")
    private String category;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MANDATORY_FLAG", nullable = false)
    @Default
    @Convert(converter = YesNoConverter.class)
    private boolean mandatory = true;

    @Column(name = "UPDATED_ALLOWED_FLAG", nullable = false)
    @Default
    @Convert(converter = YesNoConverter.class)
    private boolean updateAllowed = true;

    @Column(name = "CODE_VALUE_TYPE", nullable = false)
    private String codeValueType;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Convert(converter = YesNoConverter.class)
    @Default
    private boolean active = true;

    @Column(name = "EXPIRY_DATE")
    private LocalDate endDate;

    @Column(name = "LIST_SEQ", nullable = false)
    @Default
    private Integer listSequence = 99;
}
