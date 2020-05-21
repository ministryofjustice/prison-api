package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import java.io.Serializable;

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

    @Column(name = "INT_LOC_PROFILE_TYPE", nullable = false)
    private String profileType;

    @Column(name = "INT_LOC_PROFILE_CODE", nullable = false)
    private String profileCode;

    public boolean isAttribute() {
        return profileType != null && profileType.equals(HousingAttributeReferenceCode.DOMAIN);
    }

}