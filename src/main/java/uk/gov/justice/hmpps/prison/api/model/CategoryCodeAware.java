package uk.gov.justice.hmpps.prison.api.model;

public interface CategoryCodeAware {
    Long getBookingId();
    void setCategoryCode(String categoryCode);
}
