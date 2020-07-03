package uk.gov.justice.hmpps.prison.repository.v1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactPersonSP {

    private Long offenderContactPersonId;
    private Long personId;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;
    private String sexCode;
    private String sexDesc;
    private String relationshipTypeCode;
    private String relationshipTypeDesc;
    private String contactTypeCode;
    private String contactTypeDesc;
    private String approvedVisitorFlag;
    private String activeFlag;
    private String restrictionTypeCode;
    private String restrictionTypeDesc;
    private LocalDate restrictionEffectiveDate;
    private LocalDate restrictionExpiryDate;
    private String commentText;

}

