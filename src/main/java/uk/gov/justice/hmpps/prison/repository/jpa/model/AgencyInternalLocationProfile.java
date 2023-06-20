package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import java.io.Serializable;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(AgencyInternalLocationProfile.PK.class)
@Table(name = "AGY_INT_LOC_PROFILES")
public class AgencyInternalLocationProfile {
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {

        @Column(name = "INTERNAL_LOCATION_ID")
        private Long locationId;

        @Column(insertable = false, updatable = false, name = "INT_LOC_PROFILE_CODE")
        private String code;

        @Column(name = "INT_LOC_PROFILE_TYPE")
        private String profileType;
    }

    @Id
    private String code;

    @Id
    @Column(name = "INTERNAL_LOCATION_ID", nullable = false)
    private Long locationId;

    @Id
    @Column(name = "INT_LOC_PROFILE_TYPE", nullable = false)
    private String profileType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HousingAttributeReferenceCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "INT_LOC_PROFILE_CODE", referencedColumnName = "code"))
    })
    private HousingAttributeReferenceCode housingAttributeReferenceCode;

    public boolean isAttribute() {
        return profileType.equals(HousingAttributeReferenceCode.DOMAIN) && housingAttributeReferenceCode != null;
    }
}
