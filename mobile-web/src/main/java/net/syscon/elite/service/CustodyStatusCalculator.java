package net.syscon.elite.service;

import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.repository.CustodyStatusRecord;

import java.util.Optional;

public class CustodyStatusCalculator {

    public CustodyStatusCode CustodyStatusCodeOf(CustodyStatusRecord record) {
        if ("O".equals(record.getBooking_status())) {
            if ("Y".equals(record.getActive_flag())) {
                if ("OUT".equals(record.getDirection_code())) {
                    if ("CRT".equals(record.getMovement_type())) {
                        return CustodyStatusCode.ACTIVE_OUT_CRT;
                    }
                    if ("TAP".equals(record.getMovement_type())) {
                        return CustodyStatusCode.ACTIVE_OUT_TAP;
                    }
                }

                return CustodyStatusCode.ACTIVE_IN;
            }

            if ("N".equals(record.getActive_flag())  && "TRN".equals(record.getMovement_type())) {
                return CustodyStatusCode.IN_TRANSIT;
            }
        }

        return Optional.ofNullable(record.getMovement_type())
                .filter("REL"::equals)
                .map(mt -> Optional.ofNullable(record.getMovement_reason_code())
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
