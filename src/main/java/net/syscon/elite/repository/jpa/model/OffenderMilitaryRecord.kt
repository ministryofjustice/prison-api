package net.syscon.elite.repository.jpa.model

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_BRANCH
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_DISCP
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_DSCHRG
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_RANK
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_WZONE
import org.hibernate.annotations.*
import org.hibernate.annotations.NotFoundAction.IGNORE
import java.time.LocalDate
import javax.persistence.Embeddable
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
data class OffenderMilitaryRecord(
    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = [
      JoinColumnOrFormula(formula = JoinFormula(value = "'$MLTY_WZONE'", referencedColumnName = "domain")),
      JoinColumnOrFormula(column = JoinColumn(name = "WAR_ZONE_CODE", referencedColumnName = "code"))
    ])
    val warZone: WarZone? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas(value = [
      JoinColumnOrFormula(formula = JoinFormula(value = "'$MLTY_BRANCH'", referencedColumnName = "domain")),
      JoinColumnOrFormula(column = JoinColumn(name = "MILITARY_BRANCH_CODE", referencedColumnName = "code"))
    ])
    val militaryBranch: MilitaryBranch,
    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = [
      JoinColumnOrFormula(formula = JoinFormula(value = "'$MLTY_DSCHRG'", referencedColumnName = "domain")),
      JoinColumnOrFormula(column = JoinColumn(name = "MILITARY_DISCHARGE_CODE", referencedColumnName = "code"))
    ])
    val militaryDischarge: MilitaryDischarge? = null,
    val description: String? = null,
    val unitNumber: String? = null,
    val enlistmentLocation: String? = null,
    val dischargeLocation: String? = null,
    @Type(type = "yes_no") val selectiveServicesFlag: Boolean = false,
    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = [
      JoinColumnOrFormula(formula = JoinFormula(value = "'$MLTY_RANK'", referencedColumnName = "domain")),
      JoinColumnOrFormula(column = JoinColumn(name = "MILITARY_RANK_CODE", referencedColumnName = "code"))
    ])
    val militaryRank: MilitaryRank? = null,
    val serviceNumber: String? = null,
    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = [
      JoinColumnOrFormula(formula = JoinFormula(value = "'$MLTY_DISCP'", referencedColumnName = "domain")),
      JoinColumnOrFormula(column = JoinColumn(name = "DISCIPLINARY_ACTION_CODE", referencedColumnName = "code"))
    ])
    val disciplinaryAction: DisciplinaryAction? = null
)
