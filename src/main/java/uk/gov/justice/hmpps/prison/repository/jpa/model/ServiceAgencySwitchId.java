package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Embeddable
public class ServiceAgencySwitchId implements Serializable {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_NAME", nullable = false)
    private ExternalService externalService;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation agencyLocation;
}
