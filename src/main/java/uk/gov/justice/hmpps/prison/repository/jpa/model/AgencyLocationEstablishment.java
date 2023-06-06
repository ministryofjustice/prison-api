package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@IdClass(AgencyLocationEstablishment.Pk.class)
@Table(name = "AGY_LOC_ESTABLISHMENTS")
@ToString
public class AgencyLocationEstablishment {

    @Id
    @Column(name = "AGY_LOC_ID", nullable = false, insertable = false, updatable = false)
    private String agencyLocId;

    @Id
    @Column(name = "ESTABLISHMENT_TYPE", nullable = false, insertable = false, updatable = false)
    private String establishmentType;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private String agencyLocId;

        private String establishmentType;
    }
}
