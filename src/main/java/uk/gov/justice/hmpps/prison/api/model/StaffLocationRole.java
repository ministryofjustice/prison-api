package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Staff Details with position and role
 **/
@Schema(description = "Staff Details with position and role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@EqualsAndHashCode
@Data
@ToString
public class StaffLocationRole {
    @Schema(required = true, description = "Unique identifier for staff member.", example = "242342")
    @NotNull
    private Long staffId;

    @Schema(required = true, description = "Staff member's first name.", example = "JOHN")
    @NotBlank
    private String firstName;

    @Schema(required = true, description = "Staff member's last name.", example = "SMITH")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "Status of staff member.", example = "ACTIVE", allowableValues = {"ACTIVE","INACTIVE"})
    @NotBlank
    private String status;

    @Schema(description = "Identifier for staff member image.", example = "2342334")
    private Long thumbnailId;

    @Schema(description = "Gender of Staff Member", example = "M", allowableValues = {"M","F","NK","NS","REF"})
    private String gender;

    @Schema(description = "Date of Birth of Staff Member", example = "1970-01-02")
    private LocalDate dateOfBirth;

    @Schema(required = true, description = "Agency at which staff member is performing role.", example = "LEI")
    @NotBlank
    private String agencyId;

    @Schema(description = "Agency description.", example = "HMP Leeds")
    private String agencyDescription;

    @Schema(required = true, description = "Date from which staff member is actively performing role.", example = "2019-02-05")
    @NotNull
    private LocalDate fromDate;

    @Schema(description = "Date on which staff member stops actively performing role.", example = "2019-03-25")
    private LocalDate toDate;

    @Schema(required = true, description = "A code that defines staff member's position at agency.", example = "PRO")
    @NotBlank
    private String position;

    @Schema(description = "Description of staff member's position at agency.", example = "Prison Officer")
    private String positionDescription;

    @Schema(required = true, description = "A code that defines staff member's role at agency.", example = "KW")
    @NotBlank
    private String role;

    @Schema(description = "Description of staff member's role at agency.", example = "Key Worker")
    private String roleDescription;

    @Schema(description = "A code the defines staff member's schedule type.", example = "FT")
    private String scheduleType;

    @Schema(description = "Description of staff member's schedule type.", example = "Full Time")
    private String scheduleTypeDescription;

    @Schema(description = "Number of hours worked per week by staff member.", example = "30")
    private BigDecimal hoursPerWeek;

    public StaffLocationRole(@NotNull Long staffId, @NotBlank String firstName, @NotBlank String lastName, @NotBlank String status, Long thumbnailId, String gender, LocalDate dateOfBirth, @NotBlank String agencyId, String agencyDescription, @NotNull LocalDate fromDate, LocalDate toDate, @NotBlank String position, String positionDescription, @NotBlank String role, String roleDescription, String scheduleType, String scheduleTypeDescription, BigDecimal hoursPerWeek) {
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.thumbnailId = thumbnailId;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.agencyId = agencyId;
        this.agencyDescription = agencyDescription;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.position = position;
        this.positionDescription = positionDescription;
        this.role = role;
        this.roleDescription = roleDescription;
        this.scheduleType = scheduleType;
        this.scheduleTypeDescription = scheduleTypeDescription;
        this.hoursPerWeek = hoursPerWeek;
    }

    public StaffLocationRole() {
    }
}
