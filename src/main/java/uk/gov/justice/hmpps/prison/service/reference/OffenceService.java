package uk.gov.justice.hmpps.prison.service.reference;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceToScheduleMappingDto;
import uk.gov.justice.hmpps.prison.api.model.StatuteDto;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HOCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.HOCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceIndicatorRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StatuteRepository;
import uk.gov.justice.hmpps.prison.service.EntityAlreadyExistsException;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class OffenceService {
    private final OffenceRepository offenceRepository;
    private final HOCodeRepository hoCodeRepository;
    private final StatuteRepository statuteRepository;
    private final OffenceIndicatorRepository offenceIndicatorRepository;

    public Page<OffenceDto> getOffences(final boolean activeOnly, Pageable pageable) {
        return convertToPaginatedDto(activeOnly ? offenceRepository.findAllByActive(true, pageable)
            : offenceRepository.findAll(pageable), pageable);
    }

    public Page<OffenceDto> getOffencesThatStartWith(final String codeStartsWith, Pageable pageable) {
        return convertToPaginatedDto(offenceRepository.findAllByCodeStartsWithIgnoreCase(codeStartsWith, pageable), pageable);
    }

    public Page<OffenceDto> findOffences(final String offenceDescription, Pageable pageable) {
        return convertToPaginatedDto(offenceRepository.findAllByDescriptionLike("%" + offenceDescription + "%", pageable), pageable);
    }

    public Page<OffenceDto> findByStatute(final String statuteCode, Pageable pageable) {
        return convertToPaginatedDto(offenceRepository.findAllByStatute(Statute.builder().code(statuteCode).build(), pageable), pageable);
    }

    public Page<OffenceDto> findByHoCode(final String hoCode, Pageable pageable) {
        return convertToPaginatedDto(offenceRepository.findAllByHoCode(HOCode.builder().code(hoCode).build(), pageable), pageable);
    }

    @Transactional
    public void createHomeOfficeCodes(final List<HOCodeDto> hoCodes) {
        hoCodes.forEach(hoCodeDto ->
            hoCodeRepository.findById(hoCodeDto.getCode()).ifPresentOrElse(
                h -> log.info(format("Home Office Notifiable Offence Code %s already exists - skipping", hoCodeDto.getCode())),
                () -> createHomeOfficeCode(hoCodeDto)
            )
        );
    }

    private void createHomeOfficeCode(HOCodeDto hoCodeDto) {
        final var hoCode = HOCode.builder()
            .code(hoCodeDto.getCode())
            .description(hoCodeDto.getDescription())
            .active(hoCodeDto.getActiveFlag() != null && hoCodeDto.getActiveFlag().equals("Y"))
            .expiryDate(hoCodeDto.getExpiryDate())
            .build();
        hoCodeRepository.save(hoCode);
    }

    @Transactional
    public void createStatutes(final List<StatuteDto> statutes) {
        statutes.forEach(statuteDto ->
            statuteRepository.findById(statuteDto.getCode()).ifPresentOrElse(
                s -> log.info(format("Statute with code %s already exists - skipping", statuteDto.getCode())),
                () -> createStatute(statuteDto)
            )
        );
    }

    private void createStatute(StatuteDto statuteDto) {
        final var statute = Statute.builder()
            .code(statuteDto.getCode())
            .description(statuteDto.getDescription())
            .legislatingBodyCode(statuteDto.getLegislatingBodyCode())
            .active(statuteDto.getActiveFlag() != null && statuteDto.getActiveFlag().equals("Y"))
            .build();
        statuteRepository.save(statute);
    }

    @Transactional
    public void createOffences(final List<OffenceDto> offences) {
        offences.forEach(this::createOffence
        );
    }

    private void createOffence(OffenceDto offenceDto) {
        final var statute = statuteRepository.findById(offenceDto.getStatuteCode().getCode()).orElseThrow(
            EntityNotFoundException.withMessage("The statute with code %s doesnt exist", offenceDto.getStatuteCode().getCode())
        );
        offenceRepository.findById(new PK(offenceDto.getCode(), statute.getCode())).ifPresent(o -> {
            throw new EntityAlreadyExistsException(format("Offence with code %s already exists", offenceDto.getCode()));
        });
        final var hoCode = findHoCodeForOffence(offenceDto);
        final var offence = Offence.builder()
            .code(offenceDto.getCode())
            .description(offenceDto.getDescription())
            .severityRanking(offenceDto.getSeverityRanking())
            .statute(statute)
            .hoCode(hoCode)
            .listSequence(offenceDto.getListSequence())
            .active(offenceDto.getActiveFlag() != null && offenceDto.getActiveFlag().equals("Y"))
            .expiryDate(offenceDto.getExpiryDate())
            .build();

        offenceRepository.save(offence);
    }

    @Transactional
    public void updateOffences(final List<OffenceDto> offences) {
        offences.forEach(this::updateOffence);
    }

    private void updateOffence(OffenceDto offenceDto) {
        final var offence = offenceRepository.findById(new PK(offenceDto.getCode(), offenceDto.getStatuteCode().getCode())).orElseThrow(
            EntityNotFoundException.withMessage("The offence with code %s doesnt exist", offenceDto.getCode())
        );
        final var hoCode = findHoCodeForOffence(offenceDto);
        offence.setDescription(offenceDto.getDescription());
        offence.setSeverityRanking(offenceDto.getSeverityRanking());
        offence.setHoCode(hoCode);
        offence.setListSequence(offenceDto.getListSequence());
        offence.setActive(offenceDto.getActiveFlag() != null && offenceDto.getActiveFlag().equals("Y"));
        offence.setExpiryDate(offenceDto.getExpiryDate());

        offenceRepository.save(offence);
    }

    private HOCode findHoCodeForOffence(OffenceDto offenceDto) {
        return offenceDto.getHoCode() != null && offenceDto.getHoCode().getCode() != null ?
            hoCodeRepository.findById(offenceDto.getHoCode().getCode()).orElseThrow(
                EntityNotFoundException.withMessage("The Home Office Notifiable Offence Code %s doesnt exist", offenceDto.getStatuteCode().getCode()))
            : null;
    }

    private PageImpl<OffenceDto> convertToPaginatedDto(final Page<Offence> pageOfOffences, final Pageable pageable) {
        final var offences = pageOfOffences.stream().map(OffenceDto::transform).toList();
        return new PageImpl<>(offences, pageable, pageOfOffences.getTotalElements());
    }

    @Transactional
    public void linkOffencesToSchedules(final List<OffenceToScheduleMappingDto> offencesToSchedules) {
        final var offenceIds = offencesToSchedules.stream().map(o -> new PK(o.getOffenceCode(), o.getStatuteCode())).collect(toSet());
        final var offences = offenceRepository.findAllById(offenceIds);

        final var offencesByCode = StreamSupport.stream(offences.spliterator(), true)
            .collect(toMap(Offence::getCode, Function.identity()));

        // Filter out any if the offence doesn't exist + filter out if the offence_indicator already exists (no duplicates)
        final var offenceIndicators = offencesToSchedules.stream()
            .filter(o -> offencesByCode.containsKey(o.getOffenceCode()))
            .map(o -> OffenceIndicator.builder()
                .offence(offencesByCode.get(o.getOffenceCode()))
                .indicatorCode(o.getSchedule().getCode())
                .build()
            )
            .filter(oi -> !offenceIndicatorRepository.existsByIndicatorCodeAndOffence_Code(oi.getIndicatorCode(), oi.getOffence().getCode()))
            .toList();

        offenceIndicatorRepository.saveAll(offenceIndicators);
    }

    @Transactional
    public void unlinkOffencesFromSchedules(final List<OffenceToScheduleMappingDto> offencesToSchedules) {
        offencesToSchedules.forEach(o ->
            offenceIndicatorRepository.deleteByIndicatorCodeAndOffence_Code(o.getSchedule().getCode(), o.getOffenceCode())
        );
    }
}
