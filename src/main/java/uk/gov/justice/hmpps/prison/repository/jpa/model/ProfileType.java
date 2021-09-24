package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    @Type(type="yes_no")
    private boolean mandatory = true;

    @Column(name = "UPDATED_ALLOWED_FLAG", nullable = false)
    @Default
    @Type(type="yes_no")
    private boolean updateAllowed = true;

    @Column(name = "CODE_VALUE_TYPE", nullable = false)
    private String codeValueType;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Type(type="yes_no")
    @Default
    private boolean active = true;

    @Column(name = "EXPIRY_DATE")
    private LocalDate endDate;

    @Column(name = "LIST_SEQ", nullable = false)
    @Default
    private Integer listSequence = 99;
}
