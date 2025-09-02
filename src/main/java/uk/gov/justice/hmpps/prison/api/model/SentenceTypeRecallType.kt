package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.hmpps.prison.api.model.RecallType.FIXED_TERM_RECALL_14
import uk.gov.justice.hmpps.prison.api.model.RecallType.FIXED_TERM_RECALL_28
import uk.gov.justice.hmpps.prison.api.model.RecallType.STANDARD_RECALL
import uk.gov.justice.hmpps.prison.api.model.RecallType.STANDARD_RECALL_255

// These SentenceCalculationType values come from NOMIS - they map to offender_sentences.sentence_calc_type in NOMIS
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class SentenceTypeRecallType(
  val sentenceType: String,
  val recallType: RecallType = RecallType.NONE,
) {
  //region SDS / ORA Sentences
  ADIMP("ADIMP"),
  ADIMP_ORA("ADIMP_ORA"),
  SEC91_03("SEC91_03"),
  SEC91_03_ORA("SEC91_03_ORA"),
  SEC250("SEC250"),
  SEC250_ORA("SEC250_ORA"),
  YOI("YOI"),
  YOI_ORA("YOI_ORA"),
  //endregion

  //region Extended Determinate Sentences
  EDS18("EDS18"),
  EDS21("EDS21"),
  EDSU18("EDSU18"),
  LASPO_AR("LASPO_AR"),
  LASPO_DR("LASPO_DR"),
  //endregion

  //region SOPC Sentences
  SDOPCU18("SDOPCU18"),
  SOPC18("SOPC18"),
  SOPC21("SOPC21"),
  SEC236A("SEC236A"),
  //endregion

  //region Fine, BOTUS and DTO Sentences
  AFINE(sentenceType = "A/FINE"),
  DTO("DTO"),
  DTO_ORA("DTO_ORA"),
  BOTUS("BOTUS"),
  //endregion

  //region Standard Recall Sentences
  LR("LR", STANDARD_RECALL),
  LR_ORA("LR_ORA", STANDARD_RECALL),
  LR_SEC91_ORA("", STANDARD_RECALL),
  LRSEC250_ORA("", STANDARD_RECALL),
  LR_EDS18("", STANDARD_RECALL),
  LR_EDS21("", STANDARD_RECALL),
  LR_EDSU18("", STANDARD_RECALL),
  LR_LASPO_AR("", STANDARD_RECALL),
  LR_LASPO_DR("", STANDARD_RECALL),
  LR_SEC236A("", STANDARD_RECALL),
  LR_SOPC18("", STANDARD_RECALL),
  LR_SOPC21("", STANDARD_RECALL),
  LR_YOI_ORA("", STANDARD_RECALL),
  //endregion

  //region Fixed Term Recall Sentences
  FTR_14_ORA("14FTR_ORA", FIXED_TERM_RECALL_14),
  FTR("FTR", FIXED_TERM_RECALL_28),
  FTR_ORA("FTR_ORA", FIXED_TERM_RECALL_28),
  FTR_SCH15("FTR_SCH15", FIXED_TERM_RECALL_28),
  FTRSCH15_ORA("FTRSCH15_ORA", FIXED_TERM_RECALL_28),
  FTRSCH18("FTRSCH18", FIXED_TERM_RECALL_28),
  FTRSCH18_ORA("FTRSCH18_ORA", FIXED_TERM_RECALL_28),
  //endregion

  //region Indeterminate Sentences
  ALP_LASPO("ALP_LASPO"),
  DLP("DLP"),
  ALP("ALP"),
  ALP_CODE18("ALP_CODE18"),
  ALP_CODE21("ALP_CODE21"),
  DFL("DFL"),
  DPP("DPP"),
  HMPL("HMPL"),
  IPP("IPP"),
  LEGACY("LEGACY"),
  LIFE("LIFE"),
  LIFE_IPP(sentenceType = "LIFE/IPP"),
  MLP("MLP"),
  SEC272("SEC272"),
  SEC275("SEC275"),
  SEC93_03("SEC93_03"),
  SEC94("SEC94"),
  ZMD("ZMD"),
  SEC93("SEC93"),
  TWENTY(sentenceType = "20"),
  //endregion

  //region Indeterminate Recall Sentences
  LR_ALP("LR_ALP", STANDARD_RECALL),
  LR_ALP_CDE18("LR_ALP_CDE18", STANDARD_RECALL),
  LR_ALP_CDE21("LR_ALP_CDE21", STANDARD_RECALL),
  LR_ALP_LASPO("LR_ALP_LASPO", STANDARD_RECALL),
  LR_DLP("LR_DLP", STANDARD_RECALL),
  LR_DPP("LR_DPP", STANDARD_RECALL),
  LR_IPP("LR_IPP", STANDARD_RECALL),
  LR_LIFE("LR_LIFE", STANDARD_RECALL),
  LR_MLP("LR_MLP", STANDARD_RECALL),
  //endregion

  //region UNSUPPORTED(null) Sentence Types
  NP("NP"),
  CR("CR"),
  AR("AR"),
  EPP("EPP"),
  CIVIL("CIVIL"),
  EXT("EXT"),
  SEC91("SEC91"),
  VOO("VOO"),
  STS18("STS18"),
  STS21("STS21"),
  TISCS("TISCS"),
  YRO("YRO"),
  //endregion

  //region UNSUPPORTED(null) Recall Sentences
  FTR_HDC("FTR_HDC", FIXED_TERM_RECALL_14),
  LR_ES("LR_ES", STANDARD_RECALL),
  LR_EPP("LR_EPP", STANDARD_RECALL),
  FTR_HDC_ORA("FTR_HDC_ORA", FIXED_TERM_RECALL_28),
  FTR_14_HDC_ORA("FTR_14_HDC_ORA", FIXED_TERM_RECALL_14),
  HDR_ORA("HDR_ORA", STANDARD_RECALL_255),
  HDR("HDR", STANDARD_RECALL_255),
  CUR("CUR", STANDARD_RECALL_255),
  CUR_ORA("CUR_ORA", STANDARD_RECALL_255),
  //endregion

  //region Unidentified
  UNIDENTIFIED("UNIDENTIFIED"),
  //endregion
  ;

  companion object {
    @JvmStatic
    fun from(sentenceCalculationType: String): SentenceTypeRecallType = entries.firstOrNull { it.sentenceType == sentenceCalculationType }
      ?: entries.firstOrNull { it.name == sentenceCalculationType }
      ?: UNIDENTIFIED
  }
}
