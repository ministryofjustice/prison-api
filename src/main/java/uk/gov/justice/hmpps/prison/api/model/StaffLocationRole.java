package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Staff Details with position and role
 **/
@ApiModel(description = "Staff Details with position and role")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class StaffLocationRole {
    @ApiModelProperty(required = true, value = "Unique identifier for staff member.", position = 1, example = "242342")
    @NotNull
    private Long staffId;

    @ApiModelProperty(required = true, value = "Staff member's first name.", position = 2, example = "JOHN")
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Staff member's last name.", position = 3, example = "SMITH")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Status of staff member.", position = 4, example = "ACTIVE", allowableValues = "ACTIVE,INACTIVE")
    @NotBlank
    private String status;

    @ApiModelProperty(value = "Identifier for staff member image.", position = 5, example = "2342334")
    private Long thumbnailId;

    @ApiModelProperty(value = "Gender of Staff Member", position = 6, example = "M", allowableValues = "M,F,NK,NS,REF")
    private String gender;

    @ApiModelProperty(value = "Date of Birth of Staff Member", position = 7, example = "1970-01-02")
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "Agency at which staff member is performing role.", position = 8, example = "LEI")
    @NotBlank
    private String agencyId;

    @ApiModelProperty(value = "Agency description.", position = 9, example = "HMP Leeds")
    private String agencyDescription;

    @ApiModelProperty(required = true, value = "Date from which staff member is actively performing role.", position = 10, example = "2019-02-05")
    @NotNull
    private LocalDate fromDate;

    @ApiModelProperty(value = "Date on which staff member stops actively performing role.", position = 11, example = "2019-03-25")
    private LocalDate toDate;

    @ApiModelProperty(required = true, value = "A code that defines staff member's position at agency.", position = 12, example = "PRO")
    @NotBlank
    private String position;

    @ApiModelProperty(value = "Description of staff member's position at agency.", position = 13, example = "Prison Officer")
    private String positionDescription;

    @ApiModelProperty(required = true, value = "A code that defines staff member's role at agency.", position = 14, example = "KW")
    @NotBlank
    private String role;

    @ApiModelProperty(value = "Description of staff member's role at agency.", position = 15, example = "Key Worker")
    private String roleDescription;

    @ApiModelProperty(value = "A code the defines staff member's schedule type.", position = 16, example = "FT")
    private String scheduleType;

    @ApiModelProperty(value = "Description of staff member's schedule type.", position = 17, example = "Full Time")
    private String scheduleTypeDescription;

    @ApiModelProperty(value = "Number of hours worked per week by staff member.", position = 18, example = "30")
    private BigDecimal hoursPerWeek;

}
