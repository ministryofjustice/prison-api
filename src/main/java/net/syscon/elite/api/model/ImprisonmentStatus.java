package net.syscon.elite.api.model;

import lombok.Data;

@Data
public class ImprisonmentStatus {

    private Long bookingId;
    private String bandCode;
    private String imprisonmentStatus;
    private String legalStatus;

    public void deriveLegalStatus() {
        if (this.bandCode != null) {
            final var bandCode = Integer.parseInt(this.bandCode);
            this.legalStatus = (bandCode <= 8 || bandCode == 11) ? "Convicted" : "Remand";
        }
    }

}
