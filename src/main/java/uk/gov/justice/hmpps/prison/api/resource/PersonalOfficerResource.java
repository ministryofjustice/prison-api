package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.security.access.prepost.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.hmpps.prison.api.model.personalofficer.*;
import uk.gov.justice.hmpps.prison.service.personalofficer.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@Tag(name = "personal-officer")
@Validated
@RequestMapping(value = "${api.base.path}/personal-officer", produces = "application/json")
public class PersonalOfficerResource {

    private final PersonalOfficerService personalOfficerService;

    @Operation(hidden = true, summary = "Retrieve all allocations for a given agency")
    @GetMapping("/{agencyId}/allocation-history")
    @PreAuthorize("hasRole('ROLE_ALLOCATIONS__RO')")
    public List<PersonalOfficer> getAllocationHistory(@PathVariable("agencyId") final String agencyId) {
        return personalOfficerService.getAllocationHistory(agencyId);
    }
}
