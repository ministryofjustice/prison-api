package uk.gov.justice.hmpps.prison.api.model;

import lombok.Data;

@Data
public class ImprisonmentStatus {

    private Long bookingId;
    private String bandCode;
    private String imprisonmentStatus;
    private String description;
    private Integer imprisonStatusSeq;
    private LegalStatus legalStatus;
}
