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

    @DataProvider
    public static Object[][] custodyStatusRecords() {
        return new Object[][] {
                {  "O",     "Y",      null,    null,    Optional.ofNullable(null),       "Active-In"           },
                {  "O",     "Y",     "OUT",   "CRT",    Optional.ofNullable(null),       "Active-Out (CRT)"    },
                {  "O",     "Y",     "OUT",   "TAP",    Optional.ofNullable(null),       "Active-Out (TAP)"    },
                {  "O",     "N",      null,   "TRN",    Optional.ofNullable(null),       "In-Transit"          },
                {   null,    null,    null,   "REL",    Optional.of("UAL"),              "Active (UAL)"        }, // Unlawfully at Large
                {   null,    null,    null,   "REL",    Optional.of("UAL_ECL"),          "Active (UAL_ECL)"    }, // Early Conditional Release
                {   null,    null,    null,   "REL",    Optional.of("ESCP"),             "Active (ESCP)"       }, // European small claims procedure
                {   null,    null,    null,   "REL",    Optional.ofNullable(null),       "Inactive-Out"        },
                {  "C",      "N",     null,    null,    Optional.ofNullable(null),       "Other"               },
                {   null,    null,    null,    null,    Optional.ofNullable(null),       "Other"               },
        };
    }

    @Test
    @UseDataProvider("custodyStatusRecords")
    public void canIdentifyAnOffendersCustodyStatusCorrectly(String booking_status, String active_flag, String direction_code, String movement_type, Optional<String> movement_reason_code, String expectedCustodyStatus) {
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
                CustodyStatusCalculator.custodyStatusOf(record));
    }

}