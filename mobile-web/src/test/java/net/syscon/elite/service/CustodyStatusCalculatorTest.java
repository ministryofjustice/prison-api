package net.syscon.elite.service;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.api.support.CustodyStatusCode;
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
                {  "O",     "Y",      null,    null,     null,        CustodyStatusCode.ACTIVE_IN         },
                {  "O",     "Y",     "OUT",   "CRT",     null,        CustodyStatusCode.ACTIVE_OUT_CRT    },
                {  "O",     "Y",     "OUT",   "TAP",     null,        CustodyStatusCode.ACTIVE_OUT_TAP    },
                {  "O",     "N",      null,   "TRN",     null,        CustodyStatusCode.IN_TRANSIT        },
                {   null,    null,    null,   "REL",    "UAL",        CustodyStatusCode.ACTIVE_UAL        }, // Unlawfully at Large
                {   null,    null,    null,   "REL",    "UAL_ECL",    CustodyStatusCode.ACTIVE_UAL_ECL    }, // Early Conditional Release
                {  "O",     "N",     "OUT",   "REL",    "ESCP",       CustodyStatusCode.ACTIVE_ESCP       }, // Escaped
                {   null,    null,    null,   "REL",    "ESCP",       CustodyStatusCode.ACTIVE_ESCP       }, // Escaped
                {   null,    null,    null,   "REL",     null,        CustodyStatusCode.IN_ACTIVE_OUT     },
                {  "C",      "N",     null,    null,     null,        CustodyStatusCode.OTHER             },
                {   null,    null,    null,    null,     null,        CustodyStatusCode.OTHER             },
                {  "O",     "Y",     "IN",    "ADM",    "N",          CustodyStatusCode.ACTIVE_IN         },
                {  "O",     "N",     "OUT",   "TRN",    "NOTR",       CustodyStatusCode.IN_TRANSIT        },
                {  "O",     "Y",     "IN",    "ADM",    "RECA",       CustodyStatusCode.ACTIVE_IN         },
                {  "O",     "Y",     "OUT",   "CRT",    "CRT",        CustodyStatusCode.ACTIVE_OUT_CRT    },
                {  "O",     "Y",     "OUT",   "TAP",    "C6",         CustodyStatusCode.ACTIVE_OUT_TAP    },
        };
    }

    @Test
    @UseDataProvider("custodyStatusRecords")
    public void canIdentifyAnOffendersCustodyStatusCorrectly(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatusCode expectedCustodyStatus) {
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