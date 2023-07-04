package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Offender Summary
 **/
@SuppressWarnings("unused")
@Schema(description = "Offender Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class OffenderSummary {
    @NotNull
    private Long bookingId;

    @NotBlank
    private String offenderNo;

    private String title;

    private String suffix;

    @NotBlank
    private String firstName;

    private String middleNames;

    @NotBlank
    private String lastName;

    private String currentlyInPrison;

    private String agencyLocationId;

    private String agencyLocationDesc;

    private String internalLocationId;

    private String internalLocationDesc;

    public OffenderSummary(@NotNull Long bookingId, @NotBlank String offenderNo, String title, String suffix, @NotBlank String firstName, String middleNames, @NotBlank String lastName, String currentlyInPrison, String agencyLocationId, String agencyLocationDesc, String internalLocationId, String internalLocationDesc) {
        this.bookingId = bookingId;
        this.offenderNo = offenderNo;
        this.title = title;
        this.suffix = suffix;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.lastName = lastName;
        this.currentlyInPrison = currentlyInPrison;
        this.agencyLocationId = agencyLocationId;
        this.agencyLocationDesc = agencyLocationDesc;
        this.internalLocationId = internalLocationId;
        this.internalLocationDesc = internalLocationDesc;
    }

    public OffenderSummary() {
    }

    /**
     * A unique booking id.
     */
    @Schema(requiredMode = REQUIRED, description = "A unique booking id.")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
     * The offender's unique offender number (aka NOMS Number in the UK).
     */
    @Schema(requiredMode = REQUIRED, description = "The offender's unique offender number (aka NOMS Number in the UK).")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
     * A code representing the offender's title (from TITLE reference domain).
     */
    @Schema(description = "A code representing the offender's title (from TITLE reference domain).")
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * A code representing a suffix that is applied to offender's name (from SUFFIX reference domain).
     */
    @Schema(description = "A code representing a suffix that is applied to offender's name (from SUFFIX reference domain).")
    @JsonProperty("suffix")
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    /**
     * The offender's first name.
     */
    @Schema(requiredMode = REQUIRED, description = "The offender's first name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * The offender's middle name(s).
     */
    @Schema(description = "The offender's middle name(s).")
    @JsonProperty("middleNames")
    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(final String middleNames) {
        this.middleNames = middleNames;
    }

    /**
     * The offender's last name.
     */
    @Schema(requiredMode = REQUIRED, description = "The offender's last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * Set to Y or N to indicate if the person is currently in prison. If not set, status is not known.
     */
    @Schema(description = "Set to Y or N to indicate if the person is currently in prison. If not set, status is not known.")
    @JsonProperty("currentlyInPrison")
    public String getCurrentlyInPrison() {
        return currentlyInPrison;
    }

    public void setCurrentlyInPrison(final String currentlyInPrison) {
        this.currentlyInPrison = currentlyInPrison;
    }

    /**
     * Agency Id (if known)
     */
    @Schema(description = "Agency Id (if known)")
    @JsonProperty("agencyLocationId")
    public String getAgencyLocationId() {
        return agencyLocationId;
    }

    public void setAgencyLocationId(final String agencyLocationId) {
        this.agencyLocationId = agencyLocationId;
    }

    /**
     * Agency description (if known)
     */
    @Schema(description = "Agency description (if known)")
    @JsonProperty("agencyLocationDesc")
    public String getAgencyLocationDesc() {
        return agencyLocationDesc;
    }

    public void setAgencyLocationDesc(final String agencyLocationDesc) {
        this.agencyLocationDesc = agencyLocationDesc;
    }

    /**
     * Internal location id (if known)
     */
    @Schema(description = "Internal location id (if known)")
    @JsonProperty("internalLocationId")
    public String getInternalLocationId() {
        return internalLocationId;
    }

    public void setInternalLocationId(final String internalLocationId) {
        this.internalLocationId = internalLocationId;
    }

    /**
     * Internal location description (if known)
     */
    @Schema(description = "Internal location description (if known)")
    @JsonProperty("internalLocationDesc")
    public String getInternalLocationDesc() {
        return internalLocationDesc;
    }

    public void setInternalLocationDesc(final String internalLocationDesc) {
        this.internalLocationDesc = internalLocationDesc;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class OffenderSummary {\n");

        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  title: ").append(title).append("\n");
        sb.append("  suffix: ").append(suffix).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleNames: ").append(middleNames).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  currentlyInPrison: ").append(currentlyInPrison).append("\n");
        sb.append("  agencyLocationId: ").append(agencyLocationId).append("\n");
        sb.append("  agencyLocationDesc: ").append(agencyLocationDesc).append("\n");
        sb.append("  internalLocationId: ").append(internalLocationId).append("\n");
        sb.append("  internalLocationDesc: ").append(internalLocationDesc).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
