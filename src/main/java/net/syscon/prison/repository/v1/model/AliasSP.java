package net.syscon.prison.repository.v1.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class AliasSP {
    private String firstName;
    private String middleNames;
    private String lastName;
    private LocalDate birthDate;
}
