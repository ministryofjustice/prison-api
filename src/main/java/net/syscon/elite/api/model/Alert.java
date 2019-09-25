package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ApiModelProperty(required = true, value = "Alert Id", example = "1")
    @JsonProperty("alertId")
    @NotNull
    private Long alertId;

    @ApiModelProperty(required = true, value = "Offender booking id.", example = "14")
    @JsonProperty("bookingId")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Offender Unique Reference", example = "G3878UK")
    @JsonProperty("offenderNo")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Alert Type", example = "X")
    @JsonProperty("alertType")
    @NotBlank
    private String alertType;

    @ApiModelProperty(required = true, value = "Alert Type Description", example = "Security")
    @JsonProperty("alertTypeDescription")
    @NotBlank
    private String alertTypeDescription;

    @ApiModelProperty(required = true, value = "Alert Code", example = "XC")
    @JsonProperty("alertCode")
    @NotBlank
    private String alertCode;

    @ApiModelProperty(required = true, value = "Alert Code Description", example = "Risk to females")
    @JsonProperty("alertCodeDescription")
    @NotBlank
    private String alertCodeDescription;

    @ApiModelProperty(required = true, value = "Alert comments", example = "has a large poster on cell wall")
    @JsonProperty("comment")
    @NotBlank
    private String comment;

    @ApiModelProperty(required = true, value = "Date the alert was created", example = "2019-08-20")
    @JsonProperty("dateCreated")
    @NotNull
    private LocalDate dateCreated;

    @ApiModelProperty(value = "Date the alert expires", example = "2019-08-20")
    @JsonProperty("dateExpires")
    private LocalDate dateExpires;

    @ApiModelProperty(required = true, value = "True / False based on presence of expiry date")
    @JsonProperty("expired")
    @NotNull
    private boolean expired;

    @ApiModelProperty(required = true, value = "True / False based on alert status")
    @JsonProperty("active")
    @NotNull
    private boolean active;

    @ApiModelProperty(value = "First name of the user who added the alert", example = "John")
    @JsonProperty("addedByFirstName")
    private String addedByFirstName;

    @ApiModelProperty(value = "Last name of the user who added the alert", example = "Smith")
    @JsonProperty("addedByLastName")
    private String addedByLastName;

    @ApiModelProperty(value = "First name of the user who expired the alert", example = "John")
    @JsonProperty("expiredByFirstName")
    private String expiredByFirstName;

    @ApiModelProperty(value = "Last name of the user who expired the alert", example = "Smith")
    @JsonProperty("expiredByLastName")
    private String expiredByLastName;
}
