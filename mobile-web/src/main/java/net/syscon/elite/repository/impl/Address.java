package net.syscon.elite.repository.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class Address {
    private String agencyId;
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

}
