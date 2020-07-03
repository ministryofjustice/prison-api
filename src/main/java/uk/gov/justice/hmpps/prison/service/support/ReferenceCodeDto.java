package uk.gov.justice.hmpps.prison.service.support;

import java.util.Objects;

public abstract class ReferenceCodeDto {
    private final String domain;
    private final String code;
    private final String parentDomain;
    private final String parentCode;

    public ReferenceCodeDto(final String domain, final String code, final String parentDomain, final String parentCode) {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(code);

        this.domain = domain;
        this.code = code;
        this.parentDomain = parentDomain;
        this.parentCode = parentCode;
    }

    public ReferenceCodeDto(final String domain, final String code) {
        this(domain, code, null, null);
    }

    public String getDomain() {
        return domain;
    }

    public String getCode() {
        return code;
    }

    public String getParentDomain() {
        return parentDomain;
    }

    public String getParentCode() {
        return parentCode;
    }
}
