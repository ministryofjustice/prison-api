package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NotFound;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_PROFILE_DETAILS")
@EqualsAndHashCode(of = { "id" }, callSuper = false)
@ToString(of =  {"id", "code" })
public class OffenderProfileDetail extends AuditableEntity {
    @SuppressWarnings("unused")
    public static class OffenderProfileDetailBuilder {
        private ProfileCode code;
        private String profileCode;

        public OffenderProfileDetailBuilder code(ProfileCode code) {
            this.code = code;
            this.profileCode(code.getId().getCode());
            return this;
        }
        public OffenderProfileDetailBuilder profileCode(String profileCode) {
            this.profileCode = profileCode;
            return this;
        }

    }


    @EmbeddedId
    private PK id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class PK implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
        private OffenderBooking offenderBooking;

        @ManyToOne(optional = false)
        @NotFound(action = IGNORE)
        @JoinColumn(name = "PROFILE_TYPE", nullable = false)
        private ProfileType type;

        @Column(name = "PROFILE_SEQ", nullable = false)
        private Integer sequence;
    }

    @ManyToOne
    @JoinColumns(value = {
        @JoinColumn(name = "PROFILE_CODE", referencedColumnName = "PROFILE_CODE", insertable = false, updatable = false),
        @JoinColumn(name = "PROFILE_TYPE", referencedColumnName = "PROFILE_TYPE", insertable = false, updatable = false)
    })
    @NotFound(action = IGNORE)
    private ProfileCode code;

    public void setProfileCode(ProfileCode code) {
        this.profileCode = code.getId().getCode();
    }

    @Column(name = "PROFILE_CODE")
    private String profileCode;

    @Column(name = "CASELOAD_TYPE")
    private String caseloadType;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "LIST_SEQ", nullable = false)
    @Builder.Default
    private Integer listSequence = 99;

}
