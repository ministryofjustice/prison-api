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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LIVING_UNIT_PROFILES")
public class LivingUnitProfile {
    @Id
    @Column(name = "PROFILE_ID", nullable = false)
    private Long profileId;

    @Column(name = "LIVING_UNIT_ID", nullable = false)
    private Long livingUnitId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation agencyLocation;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "INT_LOC_PROFILE_TYPE")
    private String profileType;

    @Column(name = "INT_LOC_PROFILE_CODE")
    private String profileCode;

    public boolean isAttribute() {
        return profileType != null && profileType.equals(HousingAttributeReferenceCode.DOMAIN);
    }

}