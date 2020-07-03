package net.syscon.prison.repository.impl;

import net.syscon.util.SQLFilter;


public class DummySQLFilter implements SQLFilter {

    @Override
    public String apply(final Object o) {
        return "";
    }
}
