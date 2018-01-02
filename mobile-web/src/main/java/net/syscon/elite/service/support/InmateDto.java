package net.syscon.elite.service.support;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(of = { "bookingId" })
public class InmateDto {
    private Long bookingId;
    private String offenderNo;
    private String firstName;
    private String lastName;
    private String locationId;
    private String locationDescription;
}
