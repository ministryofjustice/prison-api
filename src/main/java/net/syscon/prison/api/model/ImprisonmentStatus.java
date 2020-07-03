package net.syscon.prison.api.model;

import lombok.Data;
import net.syscon.prison.api.model.LegalStatusCalc.LegalStatus;

@Data
public class ImprisonmentStatus {

    private Long bookingId;
    private String bandCode;
    private String imprisonmentStatus;
    private Integer imprisonStatusSeq;
    private LegalStatus legalStatus;
}
