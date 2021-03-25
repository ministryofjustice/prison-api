package uk.gov.justice.hmpps.prison.api.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.justice.hmpps.prison.api.model.LegalStatusCalc.LegalStatus;

@Builder
@Data
public class ImprisonmentStatus {

    private Long bookingId;
    private String bandCode;
    private String imprisonmentStatus;
    private String description;
    private Integer imprisonStatusSeq;
    private LegalStatus legalStatus;
}
