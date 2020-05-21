package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(LivingUnit.PK.class)
@Table(name = "LIVING_UNITS")
public class LivingUnit {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {

        @Column(name = "LIVING_UNIT_ID", insertable = false, updatable = false, nullable = false)
        private Long livingUnitId;

        @Column(name = "DESCRIPTION", nullable = false)
        private String description;

        @Column(name = "AGY_LOC_ID", nullable = false)
        private String agencyLocationId;

    }

    @Id
    @Column(name = "LIVING_UNIT_ID", nullable = false)
    private Long livingUnitId;

    @Id
    @Column(name = "AGY_LOC_ID", nullable = false)
    private String agencyLocationId;

    @Id
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "LIVING_UNIT_TYPE", nullable = false)
    private String livingUnitType;

    @Column(name = "LIVING_UNIT_CODE", nullable = false)
    private String livingUnitCode;

    @Column(name = "LEVEL_1_CODE")
    private String level1Code;

    @Column(name = "LEVEL_2_CODE")
    private String level2Code;

    @Column(name = "LEVEL_3_CODE")
    private String level3Code;

    @Column(name = "LEVEL_4_CODE")
    private String level4Code;

    @Column(name = "USER_DESC")
    private String userDescription;

    @Column(name = "ACA_CAP_RATING")
    private Integer acaCapRating;

    @Column(name = "SECURITY_LEVEL_CODE")
    private String securityLevelCode;

    @Column(name = "LIST_SEQ")
    private Integer listSeq;

    @Column(name = "PARENT_LIVING_UNIT_ID")
    private Long parentLivingUnitId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HousingUnitTypeReferenceCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "HOUSING_UNIT_TYPE", referencedColumnName = "code"))
    })
    private HousingUnitTypeReferenceCode housingUnitTypeReferenceCode;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    private String activeFlag;

    @Column(name = "CONTROL_ACTIVE_FLAG")
    private ActiveFlag controlActiveFlag;

    @Column(name = "CAPACITY")
    private Integer capacity;

    @Column(name = "OPERATION_CAPACITY")
    private Integer operationalCapacity;

    @Column(name = "CERTIFIED_FLAG", nullable = false)
    private String certifiedFlag;

    @Column(name = "DEACTIVATE_DATE")
    private LocalDate deactivateDate;

    @Column(name = "REACTIVATE_DATE")
    private LocalDate reactivateDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + LivingUnitReasonReferenceCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "DEACTIVATE_REASON_CODE", referencedColumnName = "code"))
    })
    private LivingUnitReasonReferenceCode deactiveReasonReferenceCode;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "LOWEST_LEVEL_FLAG")
    private String lowestLevelFlag;

    @Column(name = "REACH_OPER_CAPACITY_FLAG")
    private String reachedOperationalCapacityFlag;

    @Column(name = "NO_OF_OCCUPANT")
    private Integer noOfOccupants;

    public boolean isActive() {
        return activeFlag.equals("Y");
    }

    public boolean isActiveCell() {
        return isActive() && livingUnitType != null && livingUnitType.equals("CELL");
    }

    public boolean hasSpace() {
        return operationalCapacity != null && noOfOccupants != null && noOfOccupants < operationalCapacity;
    }

    public boolean isActiveCellWithSpace() {
        return isActiveCell() && hasSpace();
    }

}