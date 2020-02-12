package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

import static net.syscon.elite.repository.jpa.model.ReferenceCode.*;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderMilitaryRecord {
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

    @ManyToOne(optional = false)
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
}
