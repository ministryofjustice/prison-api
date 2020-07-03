package uk.gov.justice.hmpps.prison.repository.impl;

import uk.gov.justice.hmpps.prison.util.SQLFilter;


public class DummySQLFilter implements SQLFilter {

    @Override
    public String apply(final Object o) {
        return "";
    }
}
