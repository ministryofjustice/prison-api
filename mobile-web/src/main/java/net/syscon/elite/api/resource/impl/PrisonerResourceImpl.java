package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.resource.PrisonerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.util.MetaDataFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("prisoners")
public class PrisonerResourceImpl implements PrisonerResource {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final InmateService inmateService;

    public PrisonerResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0 || authentication.authorities.?[authority.contains('GLOBAL_SEARCH')].size() != 0")
    public GetPrisonersResponse getPrisoners(String firstName, String middleNames, String lastName, String pncNumber, String croNumber, String dob, String dobFrom, String dobTo, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {

        final PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
                .firstName(firstName)
                .middleNames(middleNames)
                .lastName(lastName)
                .pncNumber(pncNumber)
                .croNumber(croNumber)
                .dob(convertToDate(dob))
                .dobFrom(convertToDate(dobFrom))
                .dobTo(convertToDate(dobTo))
                .build();

        final List<PrisonerDetail> prisoners = inmateService.findPrisoners(criteria, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));

        return GetPrisonersResponse.respond200WithApplicationJson(prisoners, MetaDataFactory.getTotalRecords(prisoners), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    private LocalDate convertToDate(String theDate) {
        if (StringUtils.isNotBlank(theDate)) {
            return LocalDate.parse(theDate, DATE_FORMAT);
        }
        return null;
    }
}
