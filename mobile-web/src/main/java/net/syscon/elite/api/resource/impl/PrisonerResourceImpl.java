package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.resource.PrisonerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;
import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("prisoners")
public class PrisonerResourceImpl implements PrisonerResource {
    private final InmateService inmateService;

    public PrisonerResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('SYSTEM_USER')].size() != 0")
    public GetPrisonersResponse getPrisoners(String firstName, String middleNames, String lastName, String pncNumber, String croNumber, String dob, String dobFrom, String dobTo, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        PrisonerDetailSearchCriteria criteria = PrisonerDetailSearchCriteria.builder()
                .firstName(firstName)
                .middleNames(middleNames)
                .lastName(lastName)
                .pncNumber(pncNumber)
                .croNumber(croNumber)
                .dob(fromISO8601DateString(dob))
                .dobFrom(fromISO8601DateString(dobFrom))
                .dobTo(fromISO8601DateString(dobTo))
                .build();

        Page<PrisonerDetail> prisoners = inmateService.findPrisoners(
                criteria,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetPrisonersResponse.respond200WithApplicationJson(prisoners);
    }
}
