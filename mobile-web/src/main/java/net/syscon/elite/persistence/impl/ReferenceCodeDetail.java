package net.syscon.elite.persistence.impl;

import lombok.Data;

@Data
public class ReferenceCodeDetail {
    private String domain;
    private String code;
    private String description;
    private String parentDomainId;
    private String parentCode;
    private String activeFlag;

    private String subDomain;
    private String subCode;
    private String subDescription;
    private String subActiveFlag;
}
