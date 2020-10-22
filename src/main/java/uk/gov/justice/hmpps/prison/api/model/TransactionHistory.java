package uk.gov.justice.hmpps.prison.api.model;

import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
public class TransactionHistory {

    public final List<TransactionHistoryItem> items;

    public TransactionHistory(List<TransactionHistoryItem> items){
        this.items = Collections.unmodifiableList(items);
    }
}