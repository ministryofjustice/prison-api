package uk.gov.justice.hmpps.prison.api.model;

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
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @ApiModelProperty(required = true, value = "Alert Id", example = "1", position = 1)
    @JsonProperty("alertId")
    @NotNull
    private Long alertId;

    @ApiModelProperty(required = true, value = "Offender booking id.", example = "14", position = 2)
    @JsonProperty("bookingId")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Offender Unique Reference", example = "G3878UK", position = 3)
    @JsonProperty("offenderNo")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Alert Type", example = "X", position = 4)
    @JsonProperty("alertType")
    @NotBlank
    private String alertType;

    @ApiModelProperty(required = true, value = "Alert Type Description", example = "Security", position = 5)
    @JsonProperty("alertTypeDescription")
    @NotBlank
    private String alertTypeDescription;

    @ApiModelProperty(required = true, value = "Alert Code", example = "XER", position = 6)
    @JsonProperty("alertCode")
    @NotBlank
    private String alertCode;

    @ApiModelProperty(required = true, value = "Alert Code Description", example = "Escape Risk", position = 7)
    @JsonProperty("alertCodeDescription")
    @NotBlank
    private String alertCodeDescription;

    @ApiModelProperty(required = true, value = "Alert comments", example = "Profession lock pick.", position = 8)
    @JsonProperty("comment")
    @NotBlank
    private String comment;

    @ApiModelProperty(required = true, value = "Date the alert was created", example = "2019-08-20", position = 9)
    @JsonProperty("dateCreated")
    @NotNull
    private LocalDate dateCreated;

    @ApiModelProperty(value = "Date the alert expires", example = "2020-08-20", position = 10)
    @JsonProperty("dateExpires")
    private LocalDate dateExpires;

    @ApiModelProperty(required = true, value = "True / False based on presence of expiry date", example = "true", position = 11)
    @JsonProperty("expired")
    @NotNull
    private boolean expired;

    @ApiModelProperty(required = true, value = "True / False based on alert status", example = "false", position = 12)
    @JsonProperty("active")
    @NotNull
    private boolean active;

    @ApiModelProperty(value = "First name of the user who added the alert", example = "John", position = 13)
    @JsonProperty("addedByFirstName")
    private String addedByFirstName;

    @ApiModelProperty(value = "Last name of the user who added the alert", example = "Smith", position = 14)
    @JsonProperty("addedByLastName")
    private String addedByLastName;

    @ApiModelProperty(value = "First name of the user who expired the alert", example = "John", position = 15)
    @JsonProperty("expiredByFirstName")
    private String expiredByFirstName;

    @ApiModelProperty(value = "Last name of the user who expired the alert", example = "Smith", position = 16)
    @JsonProperty("expiredByLastName")
    private String expiredByLastName;
}
