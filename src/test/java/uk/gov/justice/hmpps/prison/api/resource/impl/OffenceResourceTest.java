package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceActivationDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceToScheduleMappingDto;
import uk.gov.justice.hmpps.prison.api.model.Schedule;
import uk.gov.justice.hmpps.prison.api.model.StatuteDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceIndicatorRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OffenceResourceTest extends ResourceTest {

    @Autowired
    private OffenceIndicatorRepository offenceIndicatorRepository;

    @Autowired
    private OffenceRepository offenceRepository;

    @Nested
    @DisplayName("Tests for the GET end point")
    public class GeneralOffencesTests {
        @Test
        public void testCanFindOffencesByOffenceCode() {
            final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

            final var httpEntity = createHttpEntity(token, null);

            final var response = testRestTemplate.exchange(
                "/api/offences/code/m?size=20",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "paged_offences_start_with_m.json");
        }
    }

    @Nested
    @DisplayName("Tests creation of HO Codes")
    public class CreateHOCodesTests {
        private final String maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

        @Test
        public void testWriteHoCode() {
            final var hoCodeDto = HOCodeDto
                .builder()
                .code("123/45")
                .description("123/45")
                .build();
            final var httpEntity = createHttpEntity(maintainerToken, List.of(hoCodeDto));

            final var response = postRequest(httpEntity, "/api/offences/ho-code");

            assertThatStatus(response, 201);
        }
    }

    @Nested
    @DisplayName("Tests creation of Statutes")
    public class CreateStatutesTests {
        private final String maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

        @Test
        public void testWriteStatute() {
            final var statuteDto = StatuteDto
                .builder()
                .code("123/45")
                .description("123/45")
                .legislatingBodyCode("UK")
                .build();
            final var httpEntity = createHttpEntity(maintainerToken, List.of(statuteDto));

            final var response = postRequest(httpEntity, "/api/offences/statute");

            assertThatStatus(response, 201);
        }
    }

    @Nested
    @DisplayName("Tests creation and update of an offence")
    public class CreateOrUpdateOffenceTests {
        private final String maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);
        private final StatuteDto statuteDto = StatuteDto
            .builder()
            .code("9235")
            .description("9235")
            .legislatingBodyCode("UK")
            .activeFlag("Y")
            .build();

        private final HOCodeDto hoCodeDto = HOCodeDto
            .builder()
            .code("923/99")
            .description("923/99")
            .activeFlag("Y")
            .build();

        private final OffenceDto offenceDto = OffenceDto.builder()
            .code("2XX")
            .statuteCode(statuteDto)
            .hoCode(hoCodeDto)
            .description("2XX Description")
            .severityRanking("58")
            .activeFlag("Y")
            .build();

        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testCreateOffence() {
            final var statuteHttpEntity = createHttpEntity(maintainerToken, List.of(statuteDto));
            final var offenceHttpEntity = createHttpEntity(maintainerToken, List.of(offenceDto));
            final var hoCodeEntity = createHttpEntity(maintainerToken, List.of(hoCodeDto));
            postRequest(statuteHttpEntity, "/api/offences/statute");
            postRequest(hoCodeEntity, "/api/offences/ho-code");

            final var response = postRequest(offenceHttpEntity, "/api/offences/offence");
            assertThatStatus(response, 201);

            final var getResponse = testRestTemplate.exchange(
                "/api/offences/code/2XX",
                HttpMethod.GET,
                createHttpEntity(maintainerToken, null),
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(getResponse, 200, "offence_after_create.json");
        }

        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testUpdateOffence() {
            final OffenceDto offenceDto = OffenceDto.builder()
                .code("M5")
                .statuteCode(StatuteDto.builder().code("RC86").build())
                .hoCode(HOCodeDto.builder().code("815/90").build())
                .description("Manslaughter Old UPDATED")
                .severityRanking("700")
                .activeFlag("N")
                .expiryDate(LocalDate.of(2020, 10, 13))
                .build();
            final var offenceHttpEntity = createHttpEntity(maintainerToken, List.of(offenceDto));

            final var response = putRequest(offenceHttpEntity);
            assertThatStatus(response, 204);

            final var getResponse = testRestTemplate.exchange(
                "/api/offences/code/M5",
                HttpMethod.GET,
                createHttpEntity(maintainerToken, null),
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(getResponse, 200, "offence_after_update.json");
        }
    }

    @Nested
    @DisplayName("Tests linking and unlinking of offences to schedules")
    public class LinkOffencesToSchedulesTests {
        @Sql(scripts = {"/sql/create_offence_data.sql"},
            executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testLinkAndUnlinkingOffences() {
            List<OffenceToScheduleMappingDto> mappingDtos = List.of(
                getMappingDto("COML025", Schedule.SCHEDULE_15),
                getMappingDto("STAT001", Schedule.SCHEDULE_13),
                getMappingDto("RC86355", Schedule.SCHEDULE_13),
                getMappingDto("COML025", Schedule.PCSC_SDS),
                getMappingDto("STAT001", Schedule.PCSC_SDS_PLUS),
                getMappingDto("RC86355", Schedule.PCSC_SEC_250)
            );
            linkOffencesToSchedules(mappingDtos);

            assertTrue(doesMappingExist(Schedule.SCHEDULE_15, "COML025"));
            assertTrue(doesMappingExist(Schedule.SCHEDULE_13, "STAT001"));
            assertTrue(doesMappingExist(Schedule.SCHEDULE_13, "RC86355"));
            assertTrue(doesMappingExist(Schedule.PCSC_SDS, "COML025"));
            assertTrue(doesMappingExist(Schedule.PCSC_SDS_PLUS, "STAT001"));
            assertTrue(doesMappingExist(Schedule.PCSC_SEC_250, "RC86355"));

            unlinkOffencesFromSchedules(mappingDtos);

            assertFalse(doesMappingExist(Schedule.SCHEDULE_15, "COML025"));
            assertFalse(doesMappingExist(Schedule.SCHEDULE_13, "STAT001"));
            assertFalse(doesMappingExist(Schedule.SCHEDULE_13, "RC86355"));
            assertFalse(doesMappingExist(Schedule.PCSC_SDS, "COML025"));
            assertFalse(doesMappingExist(Schedule.PCSC_SDS_PLUS, "STAT001"));
            assertFalse(doesMappingExist(Schedule.PCSC_SEC_250, "RC86355"));
        }

        private boolean doesMappingExist(Schedule schedule, String offenceCode) {
            return offenceIndicatorRepository.existsByIndicatorCodeAndOffenceCode(schedule.getCode(), offenceCode);
        }

        private OffenceToScheduleMappingDto getMappingDto(String offenceCode, Schedule schedule) {
            return OffenceToScheduleMappingDto.builder().offenceCode(offenceCode).schedule(schedule).build();
        }
    }

    @Nested
    @DisplayName("Tests for activating and deactivating of offences")
    public class ActivateOrDeactivateOffencesTests {
        @Sql(scripts = {"/sql/create_active_and_inactive_offence.sql"},
            executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testActivateOffence() {
            OffenceActivationDto inactiveOffence = new OffenceActivationDto("COML026", "COML", true);

            updateOffenceActiveFlag(inactiveOffence);

            Offence offence = offenceRepository.findById(new PK("COML026", "COML")).get();
            assertThat(offence.getCode()).isEqualTo("COML026");
            assertTrue(offence.isActive());
        }

        @Sql(scripts = {"/sql/create_active_and_inactive_offence.sql"},
            executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testDeactivateOffence() {
            OffenceActivationDto inactiveOffence = new OffenceActivationDto("COML025", "COML", false);

            updateOffenceActiveFlag(inactiveOffence);

            Offence offence = offenceRepository.findById(new PK("COML025", "COML")).get();
            assertThat(offence.getCode()).isEqualTo("COML025");
            assertFalse(offence.isActive());
        }
    }

    private ResponseEntity<String> postRequest(HttpEntity<?> httpEntity, String url) {
        return testRestTemplate.exchange(
            url,
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<>() {
            });
    }

    private void linkOffencesToSchedules(List<OffenceToScheduleMappingDto> mappings) {
        webTestClient.post().uri("/api/offences/link-to-schedule")
            .headers(setAuthorisation(List.of("ROLE_UPDATE_OFFENCE_SCHEDULES")))
            .bodyValue(mappings)
            .exchange()
            .expectStatus().isCreated();
    }

    private void unlinkOffencesFromSchedules(List<OffenceToScheduleMappingDto> mappings) {
        webTestClient.post().uri("/api/offences/unlink-from-schedule")
            .headers(setAuthorisation(List.of("ROLE_UPDATE_OFFENCE_SCHEDULES")))
            .bodyValue(mappings)
            .exchange()
            .expectStatus().isOk();
    }

    private ResponseEntity<String> putRequest(HttpEntity<?> httpEntity) {
        return testRestTemplate.exchange(
            "/api/offences/offence",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<>() {
            });
    }

    private void updateOffenceActiveFlag(OffenceActivationDto offenceActivationDto) {
        webTestClient.put().uri("/api/offences/update-active-flag")
            .headers(setAuthorisation(List.of("ROLE_NOMIS_OFFENCE_ACTIVATOR")))
            .bodyValue(offenceActivationDto)
            .exchange()
            .expectStatus()
            .isOk();
    }
}
