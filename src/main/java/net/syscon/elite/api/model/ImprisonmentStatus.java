package net.syscon.elite.api.model;

import lombok.Data;
import net.syscon.elite.api.model.LegalStatusCalc.LegalStatus;

@Data
public class ImprisonmentStatus {

    private Long bookingId;
    private String bandCode;
    private String imprisonmentStatus;
    private Integer imprisonStatusSeq;
    private LegalStatus legalStatus;
}
