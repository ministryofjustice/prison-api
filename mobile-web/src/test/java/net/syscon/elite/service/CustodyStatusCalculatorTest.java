package net.syscon.elite.service;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.repository.CustodyStatusRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;

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
                {  "O",     "Y",      null,    null,     null,        "Active-In"           },
                {  "O",     "Y",     "OUT",   "CRT",     null,        "Active-Out (CRT)"    },
                {  "O",     "Y",     "OUT",   "TAP",     null,        "Active-Out (TAP)"    },
                {  "O",     "N",      null,   "TRN",     null,        "In-Transit"          },
                {   null,    null,    null,   "REL",    "UAL",        "Active (UAL)"        }, // Unlawfully at Large
                {   null,    null,    null,   "REL",    "UAL_ECL",    "Active (UAL_ECL)"    }, // Early Conditional Release
                {  "O",     "N",     "OUT",   "REL",    "ESCP",       "Active (ESCP)"       }, // Escaped
                {   null,    null,    null,   "REL",    "ESCP",       "Active (ESCP)"       }, // Escaped
                {   null,    null,    null,   "REL",     null,        "Inactive-Out"        },
                {  "C",      "N",     null,    null,     null,        "Other"               },
                {   null,    null,    null,    null,     null,        "Other"               },
                {  "O",     "Y",     "IN",    "ADM",    "N",          "Active-In"           },
                {  "O",     "N",     "OUT",   "TRN",    "NOTR",       "In-Transit"          },
                {  "O",     "Y",     "IN",    "ADM",    "RECA",       "Active-In"           },
                {  "O",     "Y",     "OUT",   "CRT",    "CRT",        "Active-Out (CRT)"    },
                {  "O",     "Y",     "OUT",   "TAP",    "C6",         "Active-Out (TAP)"    },
        };
    }

    @Test
    @UseDataProvider("custodyStatusRecords")
    public void canIdentifyAnOffendersCustodyStatusCorrectly(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, String expectedCustodyStatus) {
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
                calculator.custodyStatusOf(record));
    }

}