package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_PROFILE_DETAILS")
@IdClass(OffenderProfileDetail.PK.class)
@EqualsAndHashCode(callSuper = false)
public class OffenderProfileDetail extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private ProfileType type;
        private Integer sequence;
    }
    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;
    @Id
    @ManyToOne(optional = false)
    @NotFound(action = IGNORE)
    @JoinColumn(name = "PROFILE_TYPE", nullable = false)
    private ProfileType type;
    @Id
    @Column(name = "PROFILE_SEQ", nullable = false)
    private Integer sequence;

    @ManyToOne(optional = false)
    @JoinColumns(value = {
        @JoinColumn(name = "PROFILE_CODE", referencedColumnName = "PROFILE_CODE", insertable = false, updatable = false),
        @JoinColumn(name = "PROFILE_TYPE", referencedColumnName = "PROFILE_TYPE", nullable = false, insertable = false, updatable = false)
    })
    @NotFound(action = IGNORE)
    private ProfileCode code;

    @Column(name = "CASELOAD_TYPE")
    private String caseloadType;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "LIST_SEQ", nullable = false)
    @Builder.Default
    private Integer listSequence = 99;

}
