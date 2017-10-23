package net.syscon.elite.service;

import net.syscon.elite.repository.CustodyStatusRecord;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CustodyStatusCalculator {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustodyStatusCalculator.class);

    public String custodyStatusOf(CustodyStatusRecord record) {
        logger.info(record.toString());

        if (record.getBooking_status() == "O") {
            if (record.getActive_flag() == "Y") {
                if (record.getDirection_code() == "OUT") {
                    return "Active-Out ("+ record.getMovement_type() +")";
                }

                return "Active-In";
            }

            if (record.getActive_flag() == "N" && record.getMovement_type() == "TRN") {
                return "In-Transit";
            }
        }

        return Optional.ofNullable(record.getMovement_type())
                .filter(mt -> mt.equals("REL"))
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
