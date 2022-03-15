package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.Lists;
import lombok.val;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.AgencyDto;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationCharge;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetailDto;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffenceDto;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Award;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AwardDto;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Hearing;
import uk.gov.justice.hmpps.prison.api.model.adjudications.HearingDto;
import uk.gov.justice.hmpps.prison.api.model.adjudications.HearingResultDto;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Sanction;
import uk.gov.justice.hmpps.prison.api.model.adjudications.SanctionDto;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.AdjudicationsRepositorySql;
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria;
import uk.gov.justice.hmpps.prison.service.support.AdjudicationChargeDto;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class AdjudicationsRepository extends RepositoryBase {

    private final RowMapper<AwardDto> rowMapper = new DataClassByColumnRowMapper<>(AwardDto.class);
    private final RowMapper<AgencyDto> agencyMapper = new DataClassByColumnRowMapper<>(AgencyDto.class);
    private final RowMapper<AdjudicationChargeDto> adjudicationMapper = new StandardBeanPropertyRowMapper<>(AdjudicationChargeDto.class);
    private final RowMapper<AdjudicationOffenceDto> offenceMapper = new DataClassByColumnRowMapper<>(AdjudicationOffenceDto.class);
    private final RowMapper<AdjudicationDetailDto> detailMapper = new DataClassByColumnRowMapper<>(AdjudicationDetailDto.class);
    private final RowMapper<HearingDto> hearingMapper = new DataClassByColumnRowMapper<>(HearingDto.class);
    private final RowMapper<HearingResultDto> resultMapper = new DataClassByColumnRowMapper<>(HearingResultDto.class);
    private final RowMapper<SanctionDto> sanctionMapper = new DataClassByColumnRowMapper<>(SanctionDto.class);


    public List<Award> findAwards(final long bookingId) {
        final var awards = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_AWARDS.getSql(), createParams("bookingId", bookingId), rowMapper);
        return awards.stream().map(AwardDto::toAward).collect(toList());
    }

    public List<Award> findAwardsForMultipleBookings(final List<Long> bookingIds) {
        final var awards = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_AWARDS_BY_BOOKINGS.getSql(), createParams("bookingIds", bookingIds), rowMapper);
        return awards.stream().map(AwardDto::toAward).collect(toList());
    }

    public List<AdjudicationOffence> findAdjudicationOffences(final String offenderNumber) {
        final var adjudications = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_LATEST_ADJUDICATION_OFFENCE_TYPES_FOR_OFFENDER.getSql(),
                createParams("offenderNo", offenderNumber),
                offenceMapper);
        return adjudications.stream().map(AdjudicationOffenceDto::toAdjudicationOffence).collect(toList());
    }


    public List<Agency> findAdjudicationAgencies(final String offenderNumber) {
        final var agencies = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_LATEST_ADJUDICATION_AGENCIES_FOR_OFFENDER.getSql(),
                createParams("offenderNo", offenderNumber),
                agencyMapper);
        return agencies.stream().map(AgencyDto::toAgency).collect(toList());
    }


    public Optional<AdjudicationDetail> findAdjudicationDetails(final String offenderNumber,
                                                                final long adjudicationNumber) {

        val details = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_ADJUDICATION.getSql(),
                createParams(
                        "offenderNo", offenderNumber,
                        "adjudicationNo", adjudicationNumber),
                detailMapper);

        return details.stream().map(detail -> populateDetails(adjudicationNumber, detail)).findFirst();
    }

    private AdjudicationDetail populateDetails(final long adjudicationNumber, final AdjudicationDetailDto detail) {

        val hearings = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_HEARINGS.getSql(), createParams("adjudicationNo", adjudicationNumber), hearingMapper);

        val hearingIds = Lists.transform(hearings, HearingDto::getOicHearingId);

        val results = getResults(hearingIds);

        val sanctions = getSanctions(hearingIds);

        val populatedHearings = hearings.stream().map(hearing ->
                populateHearing(
                        hearing,
                        results.getOrDefault(hearing.getOicHearingId(), List.of()),
                        sanctions.getOrDefault(hearing.getOicHearingId(), List.of())))
                .collect(toList());

        return detail.toAdjudicationDetail().toBuilder().hearings(populatedHearings).build();
    }

    private Hearing populateHearing(final HearingDto hearing, final List<HearingResultDto> results, final List<SanctionDto> sanctions) {

        val sanctionsByResult = sanctions.stream().map(SanctionDto::toSanction).collect(groupingBy(Sanction::getResultSeq));

        val populatedResults = results.stream().map(result ->
                result.toHearingResult().toBuilder()
                        .sanctions(sanctionsByResult.getOrDefault(result.getResultSeq(), List.of()))
                        .build())
                .collect(toList());

        return hearing.toHearing().toBuilder().results(populatedResults).build();
    }

    private Map<Long, List<SanctionDto>> getSanctions(List<Long> hearingIds) {
        return hearingIds.isEmpty()
                ? Map.of()
                : jdbcTemplate.query(AdjudicationsRepositorySql.FIND_SANCTIONS.getSql(), createParams("hearingIds", hearingIds), sanctionMapper)
                .stream()
                .collect(groupingBy(SanctionDto::getOicHearingId));
    }

    private Map<Long, List<HearingResultDto>> getResults(List<Long> hearingIds) {
        return hearingIds.isEmpty()
                ? Map.of()
                : jdbcTemplate.query(AdjudicationsRepositorySql.FIND_RESULTS.getSql(), createParams("hearingIds", hearingIds), resultMapper)
                .stream()
                .collect(groupingBy(HearingResultDto::getOicHearingId));
    }


    public Page<Adjudication> findAdjudications(final AdjudicationSearchCriteria criteria) {

        val pageRequest = criteria.getPageRequest();

        val params = createParamSource(pageRequest,
                "offenderNo", criteria.getOffenderNumber(),
                "offenceId", criteria.getOffenceId(),
                "agencyLocationId", criteria.getAgencyId(),
                "findingCode", criteria.getFindingCode(),
                "startDate", asDate(criteria.getStartDate()),
                "endDate", asDate(criteria.getEndDate()));

        val adjudicationCharges = jdbcTemplate.query(AdjudicationsRepositorySql.FIND_LATEST_ADJUDICATIONS_FOR_OFFENDER.getSql(), params, adjudicationMapper);

        val chargesGroupedByAdjudication = adjudicationCharges.stream()
                .collect(groupingBy(AdjudicationChargeDto::getAdjudicationNumber))
                .values();

        val page = chargesGroupedByAdjudication.stream()
                .map(this::toAdjudication)
                .sorted(comparing(Adjudication::getReportTime).reversed())
                .skip(criteria.getPageRequest().getOffset())
                .limit(criteria.getPageRequest().getLimit())
                .collect(toList());

        return new Page<>(page, chargesGroupedByAdjudication.size(), pageRequest);
    }


    private Adjudication toAdjudication(List<AdjudicationChargeDto> charges) {

        val firstCharge = charges.get(0);
        val convertedCharges = charges.stream().map(this::toCharge).collect(toList());

        return Adjudication.builder()
                .agencyIncidentId(firstCharge.getAgencyIncidentId())
                .partySeq(firstCharge.getPartySeq())
                .agencyId(firstCharge.getAgencyId())
                .adjudicationNumber(firstCharge.getAdjudicationNumber())
                .reportTime(firstCharge.getReportTime())
                .adjudicationCharges(convertedCharges)
                .build();
    }

    private AdjudicationCharge toCharge(final AdjudicationChargeDto charge) {
        return AdjudicationCharge.builder()
                .oicChargeId(charge.getOicChargeId())
                .offenceCode(charge.getOffenceCode())
                .offenceDescription(charge.getOffenceDescription())
                .findingCode(charge.getFindingCode())
                .build();
    }

    private SqlParameterValue asDate(final LocalDate startDate) {
        return new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(startDate));
    }
}
