package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Staff Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Staff Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StaffLocationRole {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long staffId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String status;

    private Long thumbnailId;

    @NotBlank
    private String agencyId;

    private String agencyDescription;

    @NotNull
    private LocalDate fromDate;

    private LocalDate toDate;

    @NotBlank
    private String position;

    private String positionDescription;

    @NotBlank
    private String role;

    private String roleDescription;

    private String scheduleType;

    private String scheduleTypeDescription;

    private BigDecimal hoursPerWeek;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Unique identifier for staff member.
      */
    @ApiModelProperty(required = true, value = "Unique identifier for staff member.")
    @JsonProperty("staffId")
    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    /**
      * Staff member's first name.
      */
    @ApiModelProperty(required = true, value = "Staff member's first name.")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      * Staff member's last name.
      */
    @ApiModelProperty(required = true, value = "Staff member's last name.")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
      * Status of staff member.
      */
    @ApiModelProperty(required = true, value = "Status of staff member.")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
      * Identifier for staff member image.
      */
    @ApiModelProperty(value = "Identifier for staff member image.")
    @JsonProperty("thumbnailId")
    public Long getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(Long thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    /**
      * Agency at which staff member is performing role.
      */
    @ApiModelProperty(required = true, value = "Agency at which staff member is performing role.")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    /**
      * Agency description.
      */
    @ApiModelProperty(value = "Agency description.")
    @JsonProperty("agencyDescription")
    public String getAgencyDescription() {
        return agencyDescription;
    }

    public void setAgencyDescription(String agencyDescription) {
        this.agencyDescription = agencyDescription;
    }

    /**
      * Date from which staff member is actively performing role.
      */
    @ApiModelProperty(required = true, value = "Date from which staff member is actively performing role.")
    @JsonProperty("fromDate")
    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    /**
      * Date on which staff member stops actively performing role.
      */
    @ApiModelProperty(value = "Date on which staff member stops actively performing role.")
    @JsonProperty("toDate")
    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    /**
      * A code that defines staff member's position at agency.
      */
    @ApiModelProperty(required = true, value = "A code that defines staff member's position at agency.")
    @JsonProperty("position")
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    /**
      * Description of staff member's position at agency.
      */
    @ApiModelProperty(value = "Description of staff member's position at agency.")
    @JsonProperty("positionDescription")
    public String getPositionDescription() {
        return positionDescription;
    }

    public void setPositionDescription(String positionDescription) {
        this.positionDescription = positionDescription;
    }

    /**
      * A code that defines staff member's role at agency.
      */
    @ApiModelProperty(required = true, value = "A code that defines staff member's role at agency.")
    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
      * Description of staff member's role at agency.
      */
    @ApiModelProperty(value = "Description of staff member's role at agency.")
    @JsonProperty("roleDescription")
    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    /**
      * A code the defines staff member's schedule type.
      */
    @ApiModelProperty(value = "A code the defines staff member's schedule type.")
    @JsonProperty("scheduleType")
    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    /**
      * Description of staff member's schedule type.
      */
    @ApiModelProperty(value = "Description of staff member's schedule type.")
    @JsonProperty("scheduleTypeDescription")
    public String getScheduleTypeDescription() {
        return scheduleTypeDescription;
    }

    public void setScheduleTypeDescription(String scheduleTypeDescription) {
        this.scheduleTypeDescription = scheduleTypeDescription;
    }

    /**
      * Number of hours worked per week by staff member.
      */
    @ApiModelProperty(value = "Number of hours worked per week by staff member.")
    @JsonProperty("hoursPerWeek")
    public BigDecimal getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(BigDecimal hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class StaffLocationRole {\n");
        
        sb.append("  staffId: ").append(staffId).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  status: ").append(status).append("\n");
        sb.append("  thumbnailId: ").append(thumbnailId).append("\n");
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  agencyDescription: ").append(agencyDescription).append("\n");
        sb.append("  fromDate: ").append(fromDate).append("\n");
        sb.append("  toDate: ").append(toDate).append("\n");
        sb.append("  position: ").append(position).append("\n");
        sb.append("  positionDescription: ").append(positionDescription).append("\n");
        sb.append("  role: ").append(role).append("\n");
        sb.append("  roleDescription: ").append(roleDescription).append("\n");
        sb.append("  scheduleType: ").append(scheduleType).append("\n");
        sb.append("  scheduleTypeDescription: ").append(scheduleTypeDescription).append("\n");
        sb.append("  hoursPerWeek: ").append(hoursPerWeek).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
