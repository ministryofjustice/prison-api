package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

/**
 * Alert
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @ApiModelProperty(required = true, value = "Alert Id")
    @JsonProperty("alertId")
    @NotNull
    private Long alertId;

    @ApiModelProperty(required = true, value = "Offender booking id.")
    @JsonProperty("bookingId")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Offender Unique Reference")
    @JsonProperty("offenderNo")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Alert Type")
    @JsonProperty("alertType")
    @NotBlank
    private String alertType;

    @ApiModelProperty(required = true, value = "Alert Type Description")
    @JsonProperty("alertTypeDescription")
    @NotBlank
    private String alertTypeDescription;

    @ApiModelProperty(required = true, value = "Alert Code")
    @JsonProperty("alertCode")
    @NotBlank
    private String alertCode;

    @ApiModelProperty(required = true, value = "Alert Code Description")
    @JsonProperty("alertCodeDescription")
    @NotBlank
    private String alertCodeDescription;

    @ApiModelProperty(required = true, value = "Alert comments")
    @JsonProperty("comment")
    @NotBlank
    private String comment;

    @ApiModelProperty(required = true, value = "Date Alert created")
    @JsonProperty("dateCreated")
    @NotNull
    private LocalDate dateCreated;

    @ApiModelProperty(value = "Date the alert expires")
    @JsonProperty("dateExpires")
    private LocalDate dateExpires;

    @ApiModelProperty(required = true, value = "True / False indicated expired")
    @JsonProperty("expired")
    @NotNull
    private boolean expired;

    @ApiModelProperty(required = true, value = "Status is active")
    @JsonProperty("active")
    @NotNull
    private boolean active;

    @ApiModelProperty(value = "First name of the user who added the alert")
    @JsonProperty("addedByFirstName")
    private String addedByFirstName;

    @ApiModelProperty(value = "Last name of the user who added the alert")
    @JsonProperty("addedByLastName")
    private String addedByLastName;

    @ApiModelProperty(value = "First name of the user who expired the alert")
    @JsonProperty("expiredByFirstName")
    private String expiredByFirstName;

    @ApiModelProperty(value = "Last name of the user who expired the alert")
    @JsonProperty("expiredByLastName")
    private String expiredByLastName;
}
