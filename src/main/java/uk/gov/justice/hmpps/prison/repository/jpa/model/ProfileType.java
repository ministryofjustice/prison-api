package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROFILE_TYPES")
public class ProfileType extends AuditableEntity {

    @Id
    @Column(name = "PROFILE_TYPE", nullable = false)
    private String type;

    @Column(name = "PROFILE_CATEGORY")
    private String category;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MANDATORY_FLAG", nullable = false)
    @Enumerated(EnumType.STRING)
    @Default
    private ActiveFlag mandatory = ActiveFlag.Y;

    @Column(name = "UPDATED_ALLOWED_FLAG", nullable = false)
    @Enumerated(EnumType.STRING)
    @Default
    private ActiveFlag updateAllowed = ActiveFlag.Y;

    @Column(name = "CODE_VALUE_TYPE", nullable = false)
    private String codeValueType;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Enumerated(EnumType.STRING)
    @Default
    private ActiveFlag activeFlag = ActiveFlag.Y;

    @Column(name = "EXPIRY_DATE")
    private LocalDate endDate;

    @Column(name = "LIST_SEQ", nullable = false)
    @Default
    private Integer listSequence = 99;
}
