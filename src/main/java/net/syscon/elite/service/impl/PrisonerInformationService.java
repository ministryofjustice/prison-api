package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import net.syscon.elite.api.model.PrisonerInformation;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.support.InmatesHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PrisonerInformationService {

    private final OffenderRepository offenderRepository;
    private final BookingService bookingService;
    private final InmateRepository repository;
    private final int maxBatchSize;

    public PrisonerInformationService(final OffenderRepository offenderRepository, final BookingService bookingService, final InmateRepository repository, @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.offenderRepository = offenderRepository;
        this.bookingService = bookingService;
        this.repository = repository;
        this.maxBatchSize = maxBatchSize;
    }

    public Page<PrisonerInformation> getPrisonerInformationByPrison(final @NotNull String agencyId, final PageRequest pageRequest) {
        final var page = offenderRepository.getPrisonersInPrison(agencyId, pageRequest);
        page.getItems().forEach(this::parseLocationDescription);
        final var bookingIds = page.getItems().stream().map(PrisonerInformation::getBookingId).collect(Collectors.toList());
        InmatesHelper.setReleaseDate(page.getItems(), bookingService.getBookingSentencesSummary(bookingIds));
        Lists.partition(bookingIds, maxBatchSize).forEach(bookingIdList -> InmatesHelper.setCategory(page.getItems(), repository.findAssessments(bookingIdList, "CATEGORY", Set.of())));

        return page;
    }

    private void parseLocationDescription(final PrisonerInformation prisonerInformation) {
        final var levels = StringUtils.split(prisonerInformation.getCellLocation(), "-");
        if (levels.length > 1) {
            prisonerInformation.setUnitCode1(levels[1]);
        }
        if (levels.length > 2) {
            prisonerInformation.setUnitCode2(levels[2]);
        }
        if (levels.length > 3) {
            prisonerInformation.setUnitCode3(levels[3]);
        }
        if (levels.length > 4) {
            prisonerInformation.setUnitCode3(prisonerInformation.getUnitCode3()+"-"+levels[4]);
        }
    }

}
