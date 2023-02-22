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
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    @Default
    @Convert(converter = YesNoConverter.class)
    private boolean updateAllowed = true;

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
