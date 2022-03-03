package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Key worker allocation details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Key worker allocation details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class KeyWorkerAllocationDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long bookingId;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String firstName;

    private String middleNames;

    @NotBlank
    private String lastName;

    @NotNull
    private Long staffId;

    @NotBlank
    private String agencyId;

    @NotNull
    private LocalDateTime assigned;

    @NotBlank
    private String internalLocationDesc;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Offender Booking Id
     */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
     * Offender Unique Reference
     */
    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(final String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
     * First Name
     */
    @ApiModelProperty(required = true, value = "First Name")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * Middle Name(s)
     */
    @ApiModelProperty(value = "Middle Name(s)")
    @JsonProperty("middleNames")
    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(final String middleNames) {
        this.middleNames = middleNames;
    }

    /**
     * Last Name
     */
    @ApiModelProperty(required = true, value = "Last Name")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * The key worker's Staff Id
     */
    @ApiModelProperty(required = true, value = "The key worker's Staff Id")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(final Long staffId) {
        this.staffId = staffId;
    }

    /**
     * Agency Id
     */
    @ApiModelProperty(required = true, value = "Agency Id")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(final String agencyId) {
        this.agencyId = agencyId;
    }

    /**
     * Date and time of the allocation
     */
    @ApiModelProperty(required = true, value = "Date and time of the allocation")
    @JsonProperty("assigned")
    public LocalDateTime getAssigned() {
        return assigned;
    }

    public void setAssigned(final LocalDateTime assigned) {
        this.assigned = assigned;
    }

    /**
     * Description of the location within the prison
     */
    @ApiModelProperty(required = true, value = "Description of the location within the prison")
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

        sb.append("class KeyWorkerAllocationDetail {\n");

        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleNames: ").append(middleNames).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  assigned: ").append(assigned).append("\n");
        sb.append("  internalLocationDesc: ").append(internalLocationDesc).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
