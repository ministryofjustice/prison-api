package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction.MLTY_DISCP;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch.MLTY_BRANCH;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge.MLTY_DSCHRG;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank.MLTY_RANK;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone.MLTY_WZONE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "OFFENDER_MILITARY_RECORDS")
@EqualsAndHashCode(exclude = "bookingAndSequence", callSuper = false)
@ToString(exclude = "bookingAndSequence")
public class OffenderMilitaryRecord extends AuditableEntity implements Serializable {
    @EmbeddedId
    private BookingAndSequence bookingAndSequence;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + MLTY_WZONE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "WAR_ZONE_CODE", referencedColumnName = "code"))
    })
    private WarZone warZone;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + MLTY_DSCHRG + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MILITARY_DISCHARGE_CODE", referencedColumnName = "code"))
    })
    private MilitaryDischarge militaryDischarge;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + MLTY_BRANCH + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MILITARY_BRANCH_CODE", referencedColumnName = "code"))
    })
    private MilitaryBranch militaryBranch;

    private String description;
    private String unitNumber;
    private String enlistmentLocation;
    private String dischargeLocation;
    @Type(type = "yes_no")
    @Default
    private Boolean selectiveServicesFlag = Boolean.FALSE;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + MLTY_RANK + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MILITARY_RANK_CODE", referencedColumnName = "code"))
    })
    private MilitaryRank militaryRank;
    private String serviceNumber;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + MLTY_DISCP + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "DISCIPLINARY_ACTION_CODE", referencedColumnName = "code"))
    })
    private DisciplinaryAction disciplinaryAction;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class BookingAndSequence implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "OFFENDER_BOOK_ID")
        private OffenderBooking offenderBooking;

        @Column(name = "MILITARY_SEQ", nullable = false)
        private Integer sequence;
    }
}
