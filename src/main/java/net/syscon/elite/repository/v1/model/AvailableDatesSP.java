package net.syscon.elite.repository.v1.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@Builder
@ToString
public class AvailableDatesSP {

    private LocalDate slotDate;
}
