package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.PrisonerDetailImpl;
import net.syscon.elite.v2.api.resource.PrisonerResource;
import org.apache.commons.lang3.StringUtils;

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
    public GetPrisonersResponse getPrisoners(String firstName, String middleNames, String lastName, String pncNumber, String croNumber, String dob, String dobFrom, String dobTo, String sortFields) {
        List<PrisonerDetailImpl> prisoners = inmateService.findPrisoners(firstName, middleNames, lastName, pncNumber, croNumber, convertToDate(dob), convertToDate(dobFrom), convertToDate(dobTo), sortFields);
        return GetPrisonersResponse.respond200WithApplicationJson(prisoners);
    }

    private Date convertToDate(String theDate) {
        if (StringUtils.isNotBlank(theDate)) {
            final LocalDate dobLocal = LocalDate.parse(theDate, DATE_FORMAT);
            return Date.from(dobLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

}
