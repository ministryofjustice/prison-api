package net.syscon.elite.repository.v1.model;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class LocationInformation {

    private String agyLocId;
    private String agyLocDesc;
    private String housingLocation;
    private String housingLevels;
}
