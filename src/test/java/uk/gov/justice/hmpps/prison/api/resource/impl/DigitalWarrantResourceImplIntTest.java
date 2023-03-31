package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantCourtCase;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantCharge;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantSentence;
import uk.gov.justice.hmpps.prison.api.support.BookingAdjustmentType;
import uk.gov.justice.hmpps.prison.api.support.SentenceAdjustmentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderChargeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DigitalWarrantResourceImplIntTest extends ResourceTest {
    @Autowired
    private OffenderCourtCaseRepository offenderCourtCaseRepository;

    @Autowired
    private OffenderChargeRepository offenderChargeRepository;

    @Autowired
    private OffenderSentenceRepository offenderSentenceRepository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;

    @Autowired
    private OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;
    @Test
    @Transactional(readOnly = true)
    public void createCourtCase_success() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "ITAG_USER",
            List.of("ROLE_MANAGE_DIGITAL_WARRANT"),
            WarrantCourtCase.builder()
                .agencyId("BMI")
                .caseType("A")
                .hearingType("FE")
                .caseInfoNumber("ABC123")
                .beginDate(LocalDate.of(2022, 10, 12))
                .build()
        );


        final var responseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/booking/-59/court-case",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

        assertThatStatus(responseEntity, 201);

        var created = offenderCourtCaseRepository.findById(Long.valueOf(Objects.requireNonNull(responseEntity.getBody()))).orElseGet(() -> fail("Coudn't find created court case"));

        assertThat(created.getAgencyLocation().getDescription()).isEqualTo("BIRMINGHAM");
        assertThat(created.getCaseSeq()).isEqualTo(2);
        assertThat(created.getCaseInfoNumber()).isEqualTo("ABC123");
        assertThat(created.getBeginDate()).isEqualTo(LocalDate.of(2022, 10, 12));
    }

    @Test
    @Transactional(readOnly = true)
    public void charge_success() {
        var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "RO_USER",
            List.of("ROLE_MANAGE_DIGITAL_WARRANT"),
            WarrantCharge.builder()
                .offenceCode("RV98011")
                .offenceStatue("RV98")
                .offenceDate(LocalDate.of(2022, 10, 10))
                .offenceEndDate(LocalDate.of(2022, 10, 12))
                .guilty(false)
                .courtCaseId(-59L)
                .build()
        );

        var responseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/booking/-59/charge",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

        assertThatStatus(responseEntity, 201);

        var created = offenderChargeRepository.findById(Long.valueOf(Objects.requireNonNull(responseEntity.getBody()))).orElseGet(() -> fail("Offence was not created."));

        assertThat(created.getOffence().getStatute().getCode()).isEqualTo("RV98");
        assertThat(created.getOffence().getCode()).isEqualTo("RV98011");
        assertThat(created.getDateOfOffence()).isEqualTo(LocalDate.of(2022, 10, 10));
        assertThat(created.getEndDate()).isEqualTo(LocalDate.of(2022, 10, 12));
        assertThat(created.getResultCodeOne().getDescription()).isEqualTo("Not Guilty");
        assertThat(created.getOffenderCourtCase().getId()).isEqualTo(-59L);

        var lessSeriousCharge = offenderChargeRepository.findById(-11L).get();
        assertThat(created.getMostSeriousFlag()).isEqualTo("Y");
        assertThat(lessSeriousCharge.getMostSeriousFlag()).isEqualTo("N");
    }



    @Test
    @Transactional(readOnly = true)
    public void sentence_success() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "RO_USER",
            List.of("ROLE_MANAGE_DIGITAL_WARRANT"),
            WarrantSentence.builder()
                .sentenceType("ADIMP")
                .sentenceCategory("2020")
                .sentenceDate(LocalDate.of(2022, 10, 10))
                .years(1)
                .months(2)
                .weeks(3)
                .days(4)
                .offenderChargeId(-11L)
                .courtCaseId(-59L)
                .build()
        );

        final var responseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/booking/-59/sentence",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

        assertThatStatus(responseEntity, 201);

        OffenderSentence created = offenderSentenceRepository.findById(new OffenderSentence.PK(-59L, Integer.valueOf(Objects.requireNonNull(responseEntity.getBody())))).orElseGet(() -> fail("Sentence was not created."));

        assertThat(created.getSequence()).isEqualTo(2);
        assertThat(created.getTerms().size()).isEqualTo(1);
        assertThat(created.getTerms().get(0).getYears()).isEqualTo(1);
        assertThat(created.getTerms().get(0).getMonths()).isEqualTo(2);
        assertThat(created.getTerms().get(0).getWeeks()).isEqualTo(3);
        assertThat(created.getTerms().get(0).getDays()).isEqualTo(4);
        assertThat(created.getTerms().get(0).getSentenceTermCode()).isEqualTo("IMP");

        assertThat(created.getSentenceStartDate()).isEqualTo(LocalDate.of(2022, 10, 10));
        assertThat(created.getCourtCase().getId()).isEqualTo(-59L);

        assertThat(created.getCourtOrder().getCourtDate()).isEqualTo(LocalDate.of(2022, 10, 10));


        assertThat(created.getOffenderSentenceCharges().size()).isEqualTo(1);
        assertThat(created.getOffenderSentenceCharges().get(0).getOffenderCharge().getId()).isEqualTo(-11L);

        assertThat(created.getCalculationType().getCalculationType()).isEqualTo("ADIMP");
        assertThat(created.getCalculationType().getCategory()).isEqualTo("2020");

        assertThat(created.getCourtOrder().getCourtEvent().getOutcomeReasonCode().getDescription()).isEqualTo("Imprisonment");

        assertThat(created.getOffenderBooking().getActiveImprisonmentStatus().get().getImprisonmentStatus().getStatus()).isEqualTo("SENT03");

    }

    @Test
    @Transactional(readOnly = true)
    public void courtCaseOffenceAndSentence_success() {
        final var courtRequestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "ITAG_USER",
            List.of("ROLE_MANAGE_DIGITAL_WARRANT"),
            WarrantCourtCase.builder()
                .agencyId("BMI")
                .caseType("A")
                .hearingType("FE")
                .caseInfoNumber("ABC123")
                .beginDate(LocalDate.of(2022, 10, 12))
                .build()
        );


        final var courtResponseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/booking/-60/court-case",
                HttpMethod.POST,
                courtRequestEntity,
                String.class
            );


        final var chargeRequestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "RO_USER",
            List.of("ROLE_MANAGE_DIGITAL_WARRANT"),
            WarrantCharge.builder()
                .offenceCode("RV98011")
                .offenceStatue("RV98")
                .offenceDate(LocalDate.of(2022, 10, 10))
                .offenceEndDate(LocalDate.of(2022, 10, 12))
                .guilty(false)
                .courtCaseId(Long.valueOf(Objects.requireNonNull(courtResponseEntity.getBody())))
                .build()
        );

        final var chargeResponseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/booking/-60/charge",
                HttpMethod.POST,
                chargeRequestEntity,
                String.class
            );



        final var sentenceRequestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "RO_USER",
            List.of("ROLE_MANAGE_DIGITAL_WARRANT"),
            WarrantSentence.builder()
                .sentenceType("ADIMP_ORA")
                .sentenceCategory("2003")
                .sentenceDate(LocalDate.of(2022, 10, 10))
                .years(1)
                .months(2)
                .weeks(3)
                .days(4)
                .offenderChargeId(Long.valueOf(Objects.requireNonNull(chargeResponseEntity.getBody())))
                .courtCaseId(Long.valueOf(Objects.requireNonNull(courtResponseEntity.getBody())))
                .build()
        );

        final var sentenceResponseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/booking/-60/sentence",
                HttpMethod.POST,
                sentenceRequestEntity,
                String.class
            );

        OffenderSentence sentence = offenderSentenceRepository.findById(new OffenderSentence.PK(-60L, Integer.valueOf(Objects.requireNonNull(sentenceResponseEntity.getBody())))).orElseGet(() -> fail("Sentence was not created."));

        assertThat(sentence.getCalculationType().getCalculationType()).isEqualTo("ADIMP_ORA");
        assertThat(sentence.getCalculationType().getCategory()).isEqualTo("2003");

        assertThat(sentence.getOffenderSentenceCharges().get(0).getOffenderCharge().getOffence().getStatute().getCode()).isEqualTo("RV98");
        assertThat(sentence.getOffenderSentenceCharges().get(0).getOffenderCharge().getOffence().getCode()).isEqualTo("RV98011");

        assertThat(sentence.getCourtCase().getCaseInfoNumber()).isEqualTo("ABC123");
        assertThat(sentence.getOffenderBooking().getActiveImprisonmentStatus().get().getImprisonmentStatus().getStatus()).isEqualTo("ADIMP_ORA");


        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_MANAGE_DIGITAL_WARRANT"), null);

        final var responseEntity = testRestTemplate
            .exchange(
                "/api/digital-warrant/court-date-results/Z0020XY",
                HttpMethod.GET,
                requestEntity,
                String.class
            );

        assertThatJsonFileAndStatus(responseEntity, HttpStatus.OK.value(), "digital-warrant-court-date-results.json");
    }
}
