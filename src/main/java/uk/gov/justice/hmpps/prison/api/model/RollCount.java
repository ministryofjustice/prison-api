package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Establishment roll count numbers for a housing block, wing, or reception etc.
 **/
@SuppressWarnings("unused")
@Schema(description = "Establishment roll count numbers for a housing block, wing, or reception etc.")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class RollCount {
    @NotNull
    private Long livingUnitId;

    @NotBlank
    private String livingUnitDesc;

    @NotNull
    private Integer bedsInUse;

    @NotNull
    private Integer currentlyInCell;

    @NotNull
    private Integer currentlyOut;

    @NotNull
    private Integer operationalCapacity;

    @NotNull
    private Integer netVacancies;

    @NotNull
    private Integer maximumCapacity;

    @NotNull
    private Integer availablePhysical;

    @NotNull
    private Integer outOfOrder;

    public RollCount(@NotNull Long livingUnitId, @NotBlank String livingUnitDesc, @NotNull Integer bedsInUse, @NotNull Integer currentlyInCell, @NotNull Integer currentlyOut, @NotNull Integer operationalCapacity, @NotNull Integer netVacancies, @NotNull Integer maximumCapacity, @NotNull Integer availablePhysical, @NotNull Integer outOfOrder) {
        this.livingUnitId = livingUnitId;
        this.livingUnitDesc = livingUnitDesc;
        this.bedsInUse = bedsInUse;
        this.currentlyInCell = currentlyInCell;
        this.currentlyOut = currentlyOut;
        this.operationalCapacity = operationalCapacity;
        this.netVacancies = netVacancies;
        this.maximumCapacity = maximumCapacity;
        this.availablePhysical = availablePhysical;
        this.outOfOrder = outOfOrder;
    }

    public RollCount() {}

    /**
     * Id of location
     */
    @Schema(required = true, description = "Id of location")
    @JsonProperty("livingUnitId")
    public Long getLivingUnitId() {
        return livingUnitId;
    }

    public void setLivingUnitId(final Long livingUnitId) {
        this.livingUnitId = livingUnitId;
    }

    /**
     * Wing, houseblock etc. name
     */
    @Schema(required = true, description = "Wing, houseblock etc. name")
    @JsonProperty("livingUnitDesc")
    public String getLivingUnitDesc() {
        return livingUnitDesc;
    }

    public void setLivingUnitDesc(final String livingUnitDesc) {
        this.livingUnitDesc = livingUnitDesc;
    }

    /**
     * No of residential prisoners
     */
    @Schema(required = true, description = "No of residential prisoners")
    @JsonProperty("bedsInUse")
    public Integer getBedsInUse() {
        return bedsInUse;
    }

    public void setBedsInUse(final Integer bedsInUse) {
        this.bedsInUse = bedsInUse;
    }

    /**
     * No of residential prisoners actually in
     */
    @Schema(required = true, description = "No of residential prisoners actually in")
    @JsonProperty("currentlyInCell")
    public Integer getCurrentlyInCell() {
        return currentlyInCell;
    }

    public void setCurrentlyInCell(final Integer currentlyInCell) {
        this.currentlyInCell = currentlyInCell;
    }

    /**
     * No of residential prisoners out
     */
    @Schema(required = true, description = "No of residential prisoners out")
    @JsonProperty("currentlyOut")
    public Integer getCurrentlyOut() {
        return currentlyOut;
    }

    public void setCurrentlyOut(final Integer currentlyOut) {
        this.currentlyOut = currentlyOut;
    }

    /**
     * Total capacity not including unavailable cells
     */
    @Schema(required = true, description = "Total capacity not including unavailable cells")
    @JsonProperty("operationalCapacity")
    public Integer getOperationalCapacity() {
        return operationalCapacity;
    }

    public void setOperationalCapacity(final Integer operationalCapacity) {
        this.operationalCapacity = operationalCapacity;
    }

    /**
     * Available empty beds
     */
    @Schema(required = true, description = "Available empty beds")
    @JsonProperty("netVacancies")
    public Integer getNetVacancies() {
        return netVacancies;
    }

    public void setNetVacancies(final Integer netVacancies) {
        this.netVacancies = netVacancies;
    }

    /**
     * Total capacity including unavailable cells
     */
    @Schema(required = true, description = "Total capacity including unavailable cells")
    @JsonProperty("maximumCapacity")
    public Integer getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(final Integer maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    /**
     * All empty beds
     */
    @Schema(required = true, description = "All empty beds")
    @JsonProperty("availablePhysical")
    public Integer getAvailablePhysical() {
        return availablePhysical;
    }

    public void setAvailablePhysical(final Integer availablePhysical) {
        this.availablePhysical = availablePhysical;
    }

    /**
     * No of unavailable cells
     */
    @Schema(required = true, description = "No of unavailable cells")
    @JsonProperty("outOfOrder")
    public Integer getOutOfOrder() {
        return outOfOrder;
    }

    public void setOutOfOrder(final Integer outOfOrder) {
        this.outOfOrder = outOfOrder;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class RollCount {\n");

        sb.append("  livingUnitId: ").append(livingUnitId).append("\n");
        sb.append("  livingUnitDesc: ").append(livingUnitDesc).append("\n");
        sb.append("  bedsInUse: ").append(bedsInUse).append("\n");
        sb.append("  currentlyInCell: ").append(currentlyInCell).append("\n");
        sb.append("  currentlyOut: ").append(currentlyOut).append("\n");
        sb.append("  operationalCapacity: ").append(operationalCapacity).append("\n");
        sb.append("  netVacancies: ").append(netVacancies).append("\n");
        sb.append("  maximumCapacity: ").append(maximumCapacity).append("\n");
        sb.append("  availablePhysical: ").append(availablePhysical).append("\n");
        sb.append("  outOfOrder: ").append(outOfOrder).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
