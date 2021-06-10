package uk.gov.justice.hmpps.prison.service.reference;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenceDto;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HOCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository;


@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class OffenceService {
    private final OffenceRepository repository;

    public Page<OffenceDto> getOffences(final boolean activeOnly, Pageable pageable) {
        return convertToPaginatedDto(activeOnly ? repository.findAllByActiveFlag(ActiveFlag.Y, pageable)
            : repository.findAll(pageable), pageable);
    }

    public Page<OffenceDto> findOffences(final String offenceDescription, Pageable pageable) {
        return convertToPaginatedDto(repository.findAllByDescriptionLike("%"+offenceDescription+"%", pageable), pageable);
    }

    public Page<OffenceDto> findByStatute(final String statuteCode, Pageable pageable) {
        return convertToPaginatedDto(repository.findAllByStatute(Statute.builder().code(statuteCode).build(), pageable), pageable);
    }

    public Page<OffenceDto> findByHoCode(final String hoCode, Pageable pageable) {
        return convertToPaginatedDto(repository.findAllByHoCode(HOCode.builder().code(hoCode).build(), pageable), pageable);
    }

    private PageImpl<OffenceDto> convertToPaginatedDto(final Page<Offence> pageOfOffences, final Pageable pageable) {
        final var offences = pageOfOffences.stream().map(OffenceDto::transform).toList();
        return new PageImpl<>(offences, pageable, pageOfOffences.getTotalElements());
    }


}
