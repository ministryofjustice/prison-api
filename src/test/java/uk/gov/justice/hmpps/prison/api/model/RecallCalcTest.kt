package uk.gov.justice.hmpps.prison.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RecallCalcTest {

  @ParameterizedTest(name = "sentenceType={0} is a recall type")
  @ValueSource(
    strings = [
      // Standard Recall
      "LR", "LR_ORA", "LR_SEC91_ORA", "LRSEC250_ORA",
      "LR_EDS18", "LR_EDS21", "LR_EDSU18",
      "LR_LASPO_AR", "LR_LASPO_DR", "LR_SEC236A",
      "LR_SOPC18", "LR_SOPC21", "LR_YOI_ORA",
      // Fixed Term Recall
      "14FTR_ORA",
      "FTR", "FTR_ORA", "FTR_SCH15", "FTRSCH15_ORA",
      "FTRSCH18", "FTRSCH18_ORA",
      "FTR_56ORA",
      // Indeterminate Recall
      "LR_ALP", "LR_ALP_CDE18", "LR_ALP_CDE21", "LR_ALP_LASPO",
      "LR_DLP", "LR_DPP", "LR_IPP", "LR_LIFE", "LR_MLP",
      // Unsupported Recall
      "FTR_HDC", "LR_ES", "LR_EPP",
      "FTR_HDC_ORA", "FTR_14_HDC_ORA",
      "HDR_ORA", "HDR", "CUR", "CUR_ORA",
    ],
  )
  fun shouldReturnTrueWhenSentenceTypeIsARecallCode(sentenceType: String) {
    val sentenceTerm = OffenderSentenceTerms.builder()
      .sentenceType(sentenceType)
      .build()

    val result = RecallCalc.calculate(
      1L,
      LegalStatus.CONVICTED_UNSENTENCED,
      emptyList(),
      listOf(sentenceTerm),
    )

    assertThat(result).isTrue()
  }
}
