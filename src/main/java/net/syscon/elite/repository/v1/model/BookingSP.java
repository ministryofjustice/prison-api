package net.syscon.elite.repository.v1.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class BookingSP {

    private Long offenderBookId;
    private String bookingNo;
    private LocalDate bookingBeginDate;
    private LocalDate bookingEndDate;
    private String activeFlag;
    private String agyLocId;
    private String agyLocDesc;
    private String latestBooking;
    private String housingLocation;
    private LocalDate relDate;
    private String housingLevels;
}
