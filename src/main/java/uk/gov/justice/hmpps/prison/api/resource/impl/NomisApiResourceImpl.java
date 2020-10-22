package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistory;
import uk.gov.justice.hmpps.prison.api.resource.NomisApiResource;
import uk.gov.justice.hmpps.prison.service.NomisApiService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@RestController
@RequestMapping("${api.base.path}/v1")
public class NomisApiResourceImpl implements NomisApiResource {

    private final NomisApiService nomisApiService;

    public NomisApiResourceImpl(NomisApiService nomisApiService) {
        this.nomisApiService = nomisApiService;
    }

    @Override
    public TransactionHistory getTransactionsHistory(@NotNull @Size(max = 3) String prisonId,
                                                     @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId,
                                                     @NotNull String accountCode,
                                                     @NotNull LocalDate fromDate,
                                                     LocalDate toDate) {

        return nomisApiService.getTransactionsHistory(prisonId, nomsId, accountCode, fromDate, toDate);
    }
}
