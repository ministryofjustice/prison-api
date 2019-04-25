package net.syscon.elite.repository.impl;

import lombok.val;
import net.syscon.elite.api.model.Adjudication;
import net.syscon.elite.api.model.AdjudicationCharge;
import net.syscon.elite.api.model.AdjudicationOffence;
import net.syscon.elite.api.model.Award;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.AdjudicationSearchCriteria;
import net.syscon.elite.service.support.AdjudicationChargeDto;
import net.syscon.util.DateTimeConverter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class AdjudicationsRepositoryImpl extends RepositoryBase implements AdjudicationsRepository {

    private final StandardBeanPropertyRowMapper<Award> rowMapper = new StandardBeanPropertyRowMapper<>(Award.class);
    private final StandardBeanPropertyRowMapper<AdjudicationChargeDto> adjudicationMapper = new StandardBeanPropertyRowMapper<>(AdjudicationChargeDto.class);
    private final StandardBeanPropertyRowMapper<AdjudicationOffence> offenceMapper = new StandardBeanPropertyRowMapper<>(AdjudicationOffence.class);

    @Override
    public List<Award> findAwards(final long bookingId) {
        return jdbcTemplate.query(getQuery("FIND_AWARDS"), createParams("bookingId", bookingId), rowMapper);
    }

    @Override
    public List<AdjudicationOffence> findAdjudicationOffences(final String offenderNumber) {
        return jdbcTemplate.query(getQuery("FIND_ADJUDICATION_OFFENCE_TYPES_FOR_OFFENDER"),
                createParams("offenderNo", offenderNumber),
                offenceMapper);
    }

    @Override
    public Page<Adjudication> findAdjudications(final AdjudicationSearchCriteria criteria) {

        val pageRequest = criteria.getPageRequest();

        val params = createParamSource(pageRequest,
                "offenderNo", criteria.getOffenderNumber(),
                "offenceId", criteria.getOffenceId(),
                "agencyLocationId", criteria.getAgencyId(),
                "startDate", asDate(criteria.getStartDate()),
                "endDate", asDate(criteria.getEndDate()));

        val adjudicationCharges = jdbcTemplate.query(getQuery("FIND_ADJUDICATIONS_FOR_OFFENDER"), params, adjudicationMapper);

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