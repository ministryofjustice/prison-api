package net.syscon.elite.service;

import net.syscon.elite.repository.CustodyStatusRecord;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CustodyStatusCalculator {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustodyStatusCalculator.class);

    public String custodyStatusOf(CustodyStatusRecord record) {
        logger.info(record.toString());

        if ("O".equals(record.getBooking_status())) {
            if ("Y".equals(record.getActive_flag())) {
                if ("OUT".equals(record.getDirection_code())) {
                    return "Active-Out ("+ record.getMovement_type() +")";
                }

                return "Active-In";
            }

            if ("N".equals(record.getActive_flag())  && "TRN".equals(record.getMovement_type())) {
                return "In-Transit";
            }
        }

        return Optional.ofNullable(record.getMovement_type())
                .filter("REL"::equals)
                .map(mt -> Optional.ofNullable(record.getMovement_reason_code())
                        .map(mrc -> {
                            switch (mrc) {
                                case "UAL":
                                case "UAL_ECL":
                                case "ESCP":
                                    return "Active ("+ mrc +")";
                                default:
                                    return "Inactive-Out";
                            }
                        }).orElse("Inactive-Out")
                ).orElse("Other");
    }

}
