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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Case Note Count Detail
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Case Note Count Detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteCount {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long bookingId;

    @NotBlank
    private String type;

    @NotBlank
    private String subType;

    @NotNull
    private Long count;

    private LocalDate fromDate;

    private LocalDate toDate;

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
     * Offender booking id
     */
    @ApiModelProperty(required = true, value = "Offender booking id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(final Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
     * Case note type.
     */
    @ApiModelProperty(required = true, value = "Case note type.")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Case note sub-type.
     */
    @ApiModelProperty(required = true, value = "Case note sub-type.")
    @JsonProperty("subType")
    public String getSubType() {
        return subType;
    }

    public void setSubType(final String subType) {
        this.subType = subType;
    }

    /**
     * Number of case notes of defined type and subType for offender.
     */
    @ApiModelProperty(required = true, value = "Number of case notes of defined type and subType for offender.")
    @JsonProperty("count")
    public Long getCount() {
        return count;
    }

    public void setCount(final Long count) {
        this.count = count;
    }

    /**
     * Count includes case notes occurring on or after this date (in YYYY-MM-DD format).
     */
    @ApiModelProperty(value = "Count includes case notes occurring on or after this date (in YYYY-MM-DD format).")
    @JsonProperty("fromDate")
    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(final LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Count includes case notes occurring on or before this date (in YYYY-MM-DD format).
     */
    @ApiModelProperty(value = "Count includes case notes occurring on or before this date (in YYYY-MM-DD format).")
    @JsonProperty("toDate")
    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(final LocalDate toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class CaseNoteCount {\n");

        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  type: ").append(type).append("\n");
        sb.append("  subType: ").append(subType).append("\n");
        sb.append("  count: ").append(count).append("\n");
        sb.append("  fromDate: ").append(fromDate).append("\n");
        sb.append("  toDate: ").append(toDate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
