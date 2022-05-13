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
import uk.gov.justice.hmpps.prison.api.model.StatuteDto;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HOCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.HOCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StatuteRepository;
import uk.gov.justice.hmpps.prison.service.EntityAlreadyExistsException;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;

import static java.lang.String.format;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class OffenceService {
    private final OffenceRepository offenceRepository;
    private final HOCodeRepository hoCodeRepository;
    private final StatuteRepository statuteRepository;

    public Page<OffenceDto> getOffences(final boolean activeOnly, Pageable pageable) {
        return convertToPaginatedDto(activeOnly ? offenceRepository.findAllByActive(true, pageable)
            : offenceRepository.findAll(pageable), pageable);
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
    public void createHomeOfficeCode(final HOCodeDto hoCodeDto) {
        hoCodeRepository.findById(hoCodeDto.getCode()).ifPresent(h -> {
            throw new EntityAlreadyExistsException(format("Home Office Notifiable Offence Code %s already exists", hoCodeDto.getCode()));
        });
        final var hoCode = HOCode.builder()
            .code(hoCodeDto.getCode())
            .description(hoCodeDto.getDescription())
            .active(hoCodeDto.getActiveFlag() != null && hoCodeDto.getActiveFlag().equals("Y"))
            .expiryDate(hoCodeDto.getExpiryDate())
            .build();
        hoCodeRepository.save(hoCode);
    }

    @Transactional
    public void createStatute(final StatuteDto statuteDto) {
        statuteRepository.findById(statuteDto.getCode()).ifPresent(h -> {
            throw new EntityAlreadyExistsException(format("Statute with code %s already exists", statuteDto.getCode()));
        });
        final var statute = Statute.builder()
            .code(statuteDto.getCode())
            .description(statuteDto.getDescription())
            .legislatingBodyCode(statuteDto.getLegislatingBodyCode())
            .active(statuteDto.getActiveFlag() != null && statuteDto.getActiveFlag().equals("Y"))
            .build();
        statuteRepository.save(statute);
    }

    @Transactional
    public void createOffence(final OffenceDto offenceDto) {
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
    public void updateOffence(final OffenceDto offenceDto) {
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


}
