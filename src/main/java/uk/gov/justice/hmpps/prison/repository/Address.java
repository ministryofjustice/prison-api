package uk.gov.justice.hmpps.prison.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
public class Address {
    private String agencyId;
    private String description;
    private String addressType;
    private String premise;
    private String street;
    private String locality;
    private String city;
    private String country;
    private String postalCode;
    private String phoneNo;
    private String extNo;
    private String phoneType;
    private Map<String, Object> additionalProperties;

}
