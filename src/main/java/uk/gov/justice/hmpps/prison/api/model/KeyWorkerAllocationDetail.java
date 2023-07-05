package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Key worker allocation details
 **/
@SuppressWarnings("unused")
@Schema(description = "Key worker allocation details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class KeyWorkerAllocationDetail {
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

    public KeyWorkerAllocationDetail(@NotNull Long bookingId, @NotBlank String offenderNo, @NotBlank String firstName, String middleNames, @NotBlank String lastName, @NotNull Long staffId, @NotBlank String agencyId, @NotNull LocalDateTime assigned, @NotBlank String internalLocationDesc) {
        this.bookingId = bookingId;
        this.offenderNo = offenderNo;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.lastName = lastName;
        this.staffId = staffId;
        this.agencyId = agencyId;
        this.assigned = assigned;
        this.internalLocationDesc = internalLocationDesc;
    }

    public KeyWorkerAllocationDetail() {
    }

    /**
     * Offender Booking Id
     */
    @Schema(requiredMode = REQUIRED, description = "Offender Booking Id")
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
    @Schema(requiredMode = REQUIRED, description = "Offender Unique Reference")
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
    @Schema(requiredMode = REQUIRED, description = "First Name")
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
    @Schema(description = "Middle Name(s)")
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
    @Schema(requiredMode = REQUIRED, description = "Last Name")
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
    @Schema(requiredMode = REQUIRED, description = "The key worker's Staff Id")
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
    @Schema(requiredMode = REQUIRED, description = "Agency Id")
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
    @Schema(requiredMode = REQUIRED, description = "Date and time of the allocation")
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
    @Schema(requiredMode = REQUIRED, description = "Description of the location within the prison")
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
