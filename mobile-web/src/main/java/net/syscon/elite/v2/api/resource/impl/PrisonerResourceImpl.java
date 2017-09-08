package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.v2.api.resource.PrisonerResource;
import net.syscon.util.MetaDataFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@RestResource
@Path("v2/prisoners")
public class PrisonerResourceImpl implements PrisonerResource {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final InmateService inmateService;

    public PrisonerResourceImpl(InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_GLOBAL_SEARCH')")
    public GetPrisonersResponse getPrisoners(String firstName, String middleNames, String lastName, String pncNumber, String croNumber, String dob, String dobFrom, String dobTo, String sortFields, Long offset, Long limit) {

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

        final List<PrisonerDetail> prisoners = inmateService.findPrisoners(criteria, sortFields, offset, limit);


        return GetPrisonersResponse.respond200WithApplicationJson(prisoners, offset, limit, MetaDataFactory.getTotalRecords(prisoners));
    }

    private Date convertToDate(String theDate) {
        if (StringUtils.isNotBlank(theDate)) {
            final LocalDate dobLocal = LocalDate.parse(theDate, DATE_FORMAT);
            return Date.from(dobLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

}
