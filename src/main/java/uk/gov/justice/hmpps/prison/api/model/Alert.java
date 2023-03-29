package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

/**
 * Alert
 **/
@SuppressWarnings("unused")
@Schema(description = "Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {

    @Hidden
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @Schema(description = "Alert Id", example = "1")
    @JsonProperty("alertId")
    @NotNull
    private Long alertId;

    @Schema(description = "Offender booking id.", example = "14")
    @JsonProperty("bookingId")
    @NotNull
    private Long bookingId;

    @Schema(description = "Offender Unique Reference", example = "G3878UK")
    @JsonProperty("offenderNo")
    @NotBlank
    private String offenderNo;

    @Schema(description = "Alert Type", example = "X")
    @JsonProperty("alertType")
    @NotBlank
    private String alertType;

    @Schema(description = "Alert Type Description", example = "Security")
    @JsonProperty("alertTypeDescription")
    @NotBlank
    private String alertTypeDescription;

    @Schema(description = "Alert Code", example = "XER")
    @JsonProperty("alertCode")
    @NotBlank
    private String alertCode;

    @Schema(description = "Alert Code Description", example = "Escape Risk")
    @JsonProperty("alertCodeDescription")
    @NotBlank
    private String alertCodeDescription;

    @Schema(description = "Alert comments", example = "Profession lock pick.")
    @JsonProperty("comment")
    @NotBlank
    private String comment;

    @Schema(description = "Date of the alert, which might differ to the date it was created", example = "2019-08-20")
    @JsonProperty("dateCreated")
    @NotNull
    private LocalDate dateCreated;

    @Schema(description = "Date the alert expires", example = "2020-08-20")
    @JsonProperty("dateExpires")
    private LocalDate dateExpires;

    @Schema(description = "True / False based on presence of expiry date", example = "true")
    @JsonProperty("expired")
    @NotNull
    private boolean expired;

    @Schema(description = "True / False based on alert status", example = "false")
    @JsonProperty("active")
    @NotNull
    private boolean active;

    @Schema(description = "First name of the user who added the alert", example = "John")
    @JsonProperty("addedByFirstName")
    private String addedByFirstName;

    @Schema(description = "Last name of the user who added the alert", example = "Smith")
    @JsonProperty("addedByLastName")
    private String addedByLastName;

    @Schema(description = "First name of the user who last modified the alert", example = "John")
    @JsonProperty("expiredByFirstName")
    private String expiredByFirstName;

    @Schema(description = "Last name of the user who last modified the alert", example = "Smith")
    @JsonProperty("expiredByLastName")
    private String expiredByLastName;
}
