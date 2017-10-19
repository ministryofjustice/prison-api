package net.syscon.elite.service;

import net.syscon.elite.repository.CustodyStatusRecord;

public class CustodyStatusCalculator {

    public static String custodyStatusOf(CustodyStatusRecord record) {
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

        if (record.getMovement_type() == "REL") {
            return record.getMovement_reason_code().map(
                    mrc -> {
                        switch (mrc) {
                            case "UAL":
                            case "UAL_ECL":
                            case "ESCP":
                                return "Active ("+ mrc +")";
                            default:
                                return "Inactive-Out";
                        }
                    }
            ).orElse("Inactive-Out");
        }

        return "Other";
    }

}
