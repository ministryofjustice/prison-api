package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Agency prisoner pay profile")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
public class AgyPrisonerPayProfile {

  @Schema(description = "Agency identifier", example = "MDI")
  private String agencyId;

  @Schema(description = "The start date when this pay profile took effect", example = "2022-10-01")
  private LocalDate startDate;

  @Schema(description = "The end date when this pay profile will stop taking effect", example = "2027-10-01")
  private LocalDate endDate;

  @Schema(description = "Whether automatic payments are enabled", example = "Y")
  private String autoPayFlag;

  @Schema(description = "The frequency that payroll runs for this agency (usually 1)", example = "1")
  private Integer payFrequency;

  @Schema(description = "The number of absences that are acceptable within one week", example = "5")
  private Integer weeklyAbsenceLimit;

  @Schema(description = "The minimum value for a half-day rate", example = "1.25")
  private BigDecimal minHalfDayRate;

  @Schema(description = "The maximum value for a half-day rate", example = "5.00")
  private BigDecimal maxHalfDayRate;

  @Schema(description = "The maximum value for piece work earnings", example = "6.00")
  private BigDecimal maxPieceWorkRate;

  @Schema(description = "The maximum value for a bonus award", example = "3.00")
  private BigDecimal maxBonusRate;

  @Schema(description = "The number of days allowed to backdate attendance before it locks.", example = "7")
  private Integer backdateDays;

  @Schema(description = "The default pay band to use when allocating offenders to paid activities.", example = "1")
  private String defaultPayBandCode;

  public AgyPrisonerPayProfile(
      String agyLocId,
      LocalDate startDate,
      LocalDate endDate,
      String autoPayFlag,
      Integer payFrequency,
      Integer weeklyAbsenceLimit,
      BigDecimal minHalfDayRate,
      BigDecimal maxHalfDayRate,
      BigDecimal maxPieceWorkRate,
      BigDecimal maxBonusRate,
      Integer backdateDays,
      String defaultPayBandCode
  ) {
    this.agencyId = agyLocId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.autoPayFlag = autoPayFlag;
    this.payFrequency = payFrequency;
    this.weeklyAbsenceLimit = weeklyAbsenceLimit;
    this.minHalfDayRate = minHalfDayRate;
    this.maxHalfDayRate = maxHalfDayRate;
    this.maxPieceWorkRate = maxPieceWorkRate;
    this.maxBonusRate = maxBonusRate;
    this.backdateDays = backdateDays;
    this.defaultPayBandCode = defaultPayBandCode;
  }

  public AgyPrisonerPayProfile() {
  }
}
