package uk.gov.justice.hmpps.prison.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"bookingId"})
public class InmateDto {
    private Long bookingId;
    private String offenderNo;
    private String firstName;
    private String lastName;
    private String locationId;
    private String locationDescription;
}
