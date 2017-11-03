package net.syscon.elite.service;

import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.service.support.CustodyStatusDto;

import java.util.Optional;

public class CustodyStatusCalculator {

    public CustodyStatusCode CustodyStatusCodeOf(CustodyStatusDto record) {
        if ("O".equals(record.getBookingStatus())) {
            if ("Y".equals(record.getActiveFlag())) {
                if ("OUT".equals(record.getDirectionCode())) {
                    if ("CRT".equals(record.getMovementType())) {
                        return CustodyStatusCode.ACTIVE_OUT_CRT;
                    }
                    if ("TAP".equals(record.getMovementType())) {
                        return CustodyStatusCode.ACTIVE_OUT_TAP;
                    }
                }

                return CustodyStatusCode.ACTIVE_IN;
            }

            if ("N".equals(record.getActiveFlag())  && "TRN".equals(record.getMovementType())) {
                return CustodyStatusCode.IN_TRANSIT;
            }
        }

        return Optional.ofNullable(record.getMovementType())
                .filter("REL"::equals)
                .map(mt -> Optional.ofNullable(record.getMovementReasonCode())
                        .map(mrc -> {
                            switch (mrc) {
                                case "UAL":
                                    return CustodyStatusCode.ACTIVE_UAL;
                                case "UAL_ECL":
                                    return CustodyStatusCode.ACTIVE_UAL_ECL;
                                case "ESCP":
                                    return CustodyStatusCode.ACTIVE_ESCP;
                                default:
                                    return CustodyStatusCode.IN_ACTIVE_OUT;
                            }
                        })
                        .orElse(CustodyStatusCode.IN_ACTIVE_OUT)
                )
                .orElse(CustodyStatusCode.OTHER);
    }

}
