package net.syscon.elite.repository.v1.model;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AvailableDatesSP {

    private LocalDate slotDate;
}
