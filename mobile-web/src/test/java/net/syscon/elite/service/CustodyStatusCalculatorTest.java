package net.syscon.elite.service;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.api.support.CustodyStatus;
import net.syscon.elite.repository.CustodyStatusRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link CustodyStatusCalculator}.
 */
@RunWith(DataProviderRunner.class)
public class CustodyStatusCalculatorTest {

    private CustodyStatusCalculator calculator = new CustodyStatusCalculator();

    @DataProvider
    public static Object[][] custodyStatusRecords() {
        return new Object[][] {
                {  "O",     "Y",      null,    null,     null,        CustodyStatus.ACTIVE_IN         },
                {  "O",     "Y",     "OUT",   "CRT",     null,        CustodyStatus.ACTIVE_OUT_CRT    },
                {  "O",     "Y",     "OUT",   "TAP",     null,        CustodyStatus.ACTIVE_OUT_TAP    },
                {  "O",     "N",      null,   "TRN",     null,        CustodyStatus.IN_TRANSIT        },
                {   null,    null,    null,   "REL",    "UAL",        CustodyStatus.ACTIVE_UAL        }, // Unlawfully at Large
                {   null,    null,    null,   "REL",    "UAL_ECL",    CustodyStatus.ACTIVE_UAL_ECL    }, // Early Conditional Release
                {  "O",     "N",     "OUT",   "REL",    "ESCP",       CustodyStatus.ACTIVE_ESCP       }, // Escaped
                {   null,    null,    null,   "REL",    "ESCP",       CustodyStatus.ACTIVE_ESCP       }, // Escaped
                {   null,    null,    null,   "REL",     null,        CustodyStatus.IN_ACTIVE_OUT     },
                {  "C",      "N",     null,    null,     null,        CustodyStatus.OTHER             },
                {   null,    null,    null,    null,     null,        CustodyStatus.OTHER             },
                {  "O",     "Y",     "IN",    "ADM",    "N",          CustodyStatus.ACTIVE_IN         },
                {  "O",     "N",     "OUT",   "TRN",    "NOTR",       CustodyStatus.IN_TRANSIT        },
                {  "O",     "Y",     "IN",    "ADM",    "RECA",       CustodyStatus.ACTIVE_IN         },
                {  "O",     "Y",     "OUT",   "CRT",    "CRT",        CustodyStatus.ACTIVE_OUT_CRT    },
                {  "O",     "Y",     "OUT",   "TAP",    "C6",         CustodyStatus.ACTIVE_OUT_TAP    },
        };
    }

    @Test
    @UseDataProvider("custodyStatusRecords")
    public void canIdentifyAnOffendersCustodyStatusCorrectly(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatus expectedCustodyStatus) {
        CustodyStatusRecord record = CustodyStatusRecord
                .builder()
                .booking_status(booking_status)
                .active_flag(active_flag)
                .direction_code(direction_code)
                .movement_type(movement_type)
                .movement_reason_code(movement_reason_code)
                .build();

        assertEquals(
                "Identifies correct custody status",
                expectedCustodyStatus,
                calculator.CustodyStatusCodeOf(record));
    }

}