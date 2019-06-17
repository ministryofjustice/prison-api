package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.model.v1.TransactionResponse;
import net.syscon.elite.api.resource.v1.NomisApiV1Resource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.v1.NomisApiV1Service;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;

@RestResource
@Path("/v1")
public class NomisApiV1ResourceImpl implements NomisApiV1Resource {

    private final NomisApiV1Service service;

    public NomisApiV1ResourceImpl(NomisApiV1Service service) {
        this.service = service;
    }


    @Override
    public OffenderResponse getOffender(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return new OffenderResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), service.getOffender(nomsId));

    }

    @Override
    public OffenderImageResponse getOffenderImage(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return new OffenderImageResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), service.getOffenderImage(nomsId));

    }

    @Override
    public LatestBookingLocationResponse getLatestBookingLocation(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return new LatestBookingLocationResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), service.getLatestBookingLocation(nomsId));
    }

    @Override
    public CreateTransactionResponse createTransaction(final String clientName,
                                                       @NotNull @Length(max = 3) final String prisonId,
                                                       @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId,
                                                       final @NotNull @Valid CreateTransaction createTransaction) {


        var uniqueClientId = StringUtils.isNotBlank(clientName) ? clientName + "-" + createTransaction.getClient_unique_ref() : createTransaction.getClient_unique_ref();

        var result = service.createTransaction(prisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(), createTransaction.getClient_transaction_id(), uniqueClientId);

        return new CreateTransactionResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), TransactionResponse.builder().id(result).build());
    }

}
