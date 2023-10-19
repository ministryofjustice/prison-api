package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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
    private Integer outOfLivingUnits;

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

    public RollCount(@NotNull Long livingUnitId, @NotBlank String livingUnitDesc,
                     @NotNull Integer bedsInUse, @NotNull Integer currentlyInCell,
                     @NotNull Integer outOfLivingUnits, @NotNull Integer currentlyOut, @NotNull Integer operationalCapacity,
                     @NotNull Integer netVacancies, @NotNull Integer maximumCapacity,
                     @NotNull Integer availablePhysical, @NotNull Integer outOfOrder) {
        this.livingUnitId = livingUnitId;
        this.livingUnitDesc = livingUnitDesc;
        this.bedsInUse = bedsInUse;
        this.currentlyInCell = currentlyInCell;
        this.outOfLivingUnits = outOfLivingUnits;
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
    @Schema(requiredMode = REQUIRED, description = "Id of location")
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
    @Schema(requiredMode = REQUIRED, description = "Wing, houseblock etc. name")
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
    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners")
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
    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners actually in")
    @JsonProperty("currentlyInCell")
    public Integer getCurrentlyInCell() {
        return currentlyInCell;
    }

    public void setCurrentlyInCell(final Integer currentlyInCell) {
        this.currentlyInCell = currentlyInCell;
    }

    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners in internal locations")
    @JsonProperty("outOfLivingUnits")
    public Integer getOutOfLivingUnits() {
        return outOfLivingUnits;
    }

    public void setOutOfLivingUnits(final Integer outOfLivingUnits) {
        this.outOfLivingUnits = outOfLivingUnits;
    }

    /**
     * No of residential prisoners out
     */
    @Schema(requiredMode = REQUIRED, description = "No of residential prisoners out")
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
    @Schema(requiredMode = REQUIRED, description = "Total capacity not including unavailable cells")
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
    @Schema(requiredMode = REQUIRED, description = "Available empty beds")
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
    @Schema(requiredMode = REQUIRED, description = "Total capacity including unavailable cells")
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
    @Schema(requiredMode = REQUIRED, description = "All empty beds")
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
    @Schema(requiredMode = REQUIRED, description = "No of unavailable cells")
    @JsonProperty("outOfOrder")
    public Integer getOutOfOrder() {
        return outOfOrder;
    }

    public void setOutOfOrder(final Integer outOfOrder) {
        this.outOfOrder = outOfOrder;
    }

    @Override
    public String toString() {
        return "class RollCount {\n" +
            "  livingUnitId: " + livingUnitId + "\n" +
            "  livingUnitDesc: " + livingUnitDesc + "\n" +
            "  bedsInUse: " + bedsInUse + "\n" +
            "  currentlyInCell: " + currentlyInCell + "\n" +
            "  currentlyOut: " + currentlyOut + "\n" +
            "  outOfLivingUnits: " + outOfLivingUnits + "\n" +
            "  operationalCapacity: " + operationalCapacity + "\n" +
            "  netVacancies: " + netVacancies + "\n" +
            "  maximumCapacity: " + maximumCapacity + "\n" +
            "  availablePhysical: " + availablePhysical + "\n" +
            "  outOfOrder: " + outOfOrder + "\n" +
            "}\n";
    }
}
