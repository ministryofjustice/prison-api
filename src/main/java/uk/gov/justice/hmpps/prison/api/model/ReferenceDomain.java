package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;

/**
 * Reference Domain
 **/
@SuppressWarnings("unused")
@Schema(description = "Reference Domain")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class ReferenceDomain {
    @NotBlank
    private String domain;

    @NotBlank
    private String description;

    @NotBlank
    private String domainStatus;

    @NotBlank
    private String ownerCode;

    @NotBlank
    private String applnCode;

    private String parentDomain;

    public ReferenceDomain(@NotBlank String domain, @NotBlank String description, @NotBlank String domainStatus, @NotBlank String ownerCode, @NotBlank String applnCode, String parentDomain) {
        this.domain = domain;
        this.description = description;
        this.domainStatus = domainStatus;
        this.ownerCode = ownerCode;
        this.applnCode = applnCode;
        this.parentDomain = parentDomain;
    }

    public ReferenceDomain() {
    }

    /**
     * Reference domain name
     */
    @Schema(required = true, description = "Reference domain name")
    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * Reference domain description.
     */
    @Schema(required = true, description = "Reference domain description.")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Reference domain status.
     */
    @Schema(required = true, description = "Reference domain status.")
    @JsonProperty("domainStatus")
    public String getDomainStatus() {
        return domainStatus;
    }

    public void setDomainStatus(final String domainStatus) {
        this.domainStatus = domainStatus;
    }

    /**
     * Reference domain owner.
     */
    @Schema(required = true, description = "Reference domain owner.")
    @JsonProperty("ownerCode")
    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(final String ownerCode) {
        this.ownerCode = ownerCode;
    }

    /**
     * Application that uses reference domain.
     */
    @Schema(required = true, description = "Application that uses reference domain.")
    @JsonProperty("applnCode")
    public String getApplnCode() {
        return applnCode;
    }

    public void setApplnCode(final String applnCode) {
        this.applnCode = applnCode;
    }

    /**
     * Parent domain for reference domain.
     */
    @Schema(description = "Parent domain for reference domain.")
    @JsonProperty("parentDomain")
    public String getParentDomain() {
        return parentDomain;
    }

    public void setParentDomain(final String parentDomain) {
        this.parentDomain = parentDomain;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class ReferenceDomain {\n");

        sb.append("  domain: ").append(domain).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  domainStatus: ").append(domainStatus).append("\n");
        sb.append("  ownerCode: ").append(ownerCode).append("\n");
        sb.append("  applnCode: ").append(applnCode).append("\n");
        sb.append("  parentDomain: ").append(parentDomain).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
