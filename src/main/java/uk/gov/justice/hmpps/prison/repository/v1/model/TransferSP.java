package uk.gov.justice.hmpps.prison.repository.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription;

@Data
@AllArgsConstructor
@ToString
public class TransferSP {
    private CodeDescription currentLocation;
    private TransactionSP transaction;

    @Data
    @AllArgsConstructor
    @ToString
    public static class TransactionSP {
        private String id;
    }
}

