package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROFILE_CODES")
@EqualsAndHashCode(of = { "id"}, callSuper = false)
@ToString(of = { "id", "description" })
public class ProfileCode extends AuditableEntity {

    @EmbeddedId
    private ProfileCode.PK id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class PK implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "PROFILE_TYPE", nullable = false)
        private ProfileType type;

        @Column(name = "PROFILE_CODE", nullable = false)
        private String code;
    }

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "UPDATE_ALLOWED_FLAG", nullable = false)
    @Enumerated(EnumType.STRING)
    @Default
    private ActiveFlag updateAllowed = ActiveFlag.Y;

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
