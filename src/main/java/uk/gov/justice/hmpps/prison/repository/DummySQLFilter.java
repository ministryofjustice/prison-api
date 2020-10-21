package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.util.SQLFilter;


public class DummySQLFilter implements SQLFilter {


    public String apply(final Object o) {
        return "";
    }
}
