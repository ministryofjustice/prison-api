package uk.gov.justice.hmpps.prison.repository;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReferenceCodeDetail {
    private String domain;
    private String code;
    private String description;
    private String parentDomain;
    private String parentCode;
    private String activeFlag;
    private Integer listSeq;
    private String systemDataFlag;
    private LocalDate expiredDate;

    private String subDomain;
    private String subCode;
    private String subDescription;
    private String subActiveFlag;
    private Integer subListSeq;
    private String subSystemDataFlag;
    private LocalDate subExpiredDate;


}
