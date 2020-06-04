package net.syscon.elite.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class OffenderBookingIdSeq {
    private String offenderNo;
    private Long bookingId;
    private Integer bookingSeq;
}
