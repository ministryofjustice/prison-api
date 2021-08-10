package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.REASON;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TYPE;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "OFFENDER_RELEASE_DETAILS")
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

}
