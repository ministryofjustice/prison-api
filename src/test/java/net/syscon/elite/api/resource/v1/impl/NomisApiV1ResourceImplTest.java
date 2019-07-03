package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CodeDescription;
import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.model.v1.Transaction;
import net.syscon.elite.api.model.v1.Transfer;
import net.syscon.elite.repository.v1.model.TransferSP;
import net.syscon.elite.repository.v1.model.TransferSP.TransactionSP;
import net.syscon.elite.service.v1.NomisApiV1Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NomisApiV1ResourceImplTest {
    @Mock
    private NomisApiV1Service service;

    private NomisApiV1ResourceImpl nomisApiV1Resource;

    @Before
    public void setUp() {
        nomisApiV1Resource = new NomisApiV1ResourceImpl(service);
    }

    @Test
    public void transferTransaction() {
        when(service.transferTransaction(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString())).
                thenReturn(new TransferSP(CodeDescription.safeNullBuild("someCode", "desc"), new TransactionSP("someId")));
        final var createTransaction = new CreateTransaction();
        createTransaction.setAmount(1234L);
        createTransaction.setClientUniqueRef("clientRef");
        createTransaction.setDescription("desc");
        createTransaction.setType("type");
        createTransaction.setClientTransactionId("transId");

        final var transfer = nomisApiV1Resource.transferTransaction("client", "previous", "nomis", createTransaction);
        assertThat(transfer).isEqualTo(new Transfer(CodeDescription.safeNullBuild("someCode", "desc"), new Transaction("someId")));
    }

    @Test
    public void createTransaction() {
        when(service.createTransaction(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString())).
                thenReturn("someId");
        final var createTransaction = new CreateTransaction();
        createTransaction.setAmount(1234L);
        createTransaction.setClientUniqueRef("clientRef");
        createTransaction.setDescription("desc");
        createTransaction.setType("type");
        createTransaction.setClientTransactionId("transId");

        final var transfer = nomisApiV1Resource.createTransaction("client", "previous", "nomis", createTransaction);
        assertThat(transfer).isEqualTo(new Transaction("someId"));
    }
}
