package net.syscon.elite.service.support;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "bookingId" })
public class InmateDto {
    private Long bookingId;
    private String offenderNo;
    private String firstName;
    private String lastName;
    private String locationId;
    private String locationDescription;
}
