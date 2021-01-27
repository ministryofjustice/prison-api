package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(LivingUnitProfile.PK.class)
@Table(name = "LIVING_UNIT_PROFILES")
public class LivingUnitProfile {
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {

        @Column(name = "LIVING_UNIT_ID")
        private Long livingUnitId;

        @Column(name = "DESCRIPTION")
        private String description;

        @Column(name = "AGY_LOC_ID")
        private String agencyLocationId;

        @Column(name = "PROFILE_ID")
        private Long profileId;

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

    @Id
    @Column(name = "PROFILE_ID")
    private Long profileId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HousingAttributeReferenceCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "INT_LOC_PROFILE_CODE", referencedColumnName = "code"))
    })
    private HousingAttributeReferenceCode housingAttributeReferenceCode;

    public boolean isAttribute() {
        return housingAttributeReferenceCode != null;
    }

}