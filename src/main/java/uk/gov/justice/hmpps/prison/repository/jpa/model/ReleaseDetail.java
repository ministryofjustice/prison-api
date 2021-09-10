package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.REASON;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TYPE;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OFFENDER_RELEASE_DETAILS")
@BatchSize(size = 25)
public class ReleaseDetail extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    private OffenderBooking booking;

    @Column(name = "RELEASE_DATE")
    private LocalDate releaseDate;

    @Column(name = "COMMENT_TEXT")
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_TYPE", referencedColumnName = "code"))
    })
    private MovementType movementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + REASON + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_REASON_CODE", referencedColumnName = "code"))
    })
    private MovementReason movementReason;

    @Column(name = "APPROVED_RELEASE_DATE")
    private LocalDate approvedReleaseDate;

    @Column(name = "AUTO_RELEASE_DATE")
    private LocalDate autoReleaseDate;

    @Column(name = "DTO_APPROVED_DATE")
    private LocalDate dtoApprovedDate;

    @Column(name = "DTO_MID_TERM_DATE")
    private LocalDate dtoMidTermDate;

    @Column(name = "VERIFIED_FLAG")
    @Enumerated(EnumType.STRING)
    private ActiveFlag verified;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final ReleaseDetail that = (ReleaseDetail) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
