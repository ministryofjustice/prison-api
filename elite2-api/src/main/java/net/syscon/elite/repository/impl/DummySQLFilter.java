package net.syscon.elite.repository.impl;

import net.syscon.util.SQLFilter;


public class DummySQLFilter implements SQLFilter {

	@Override
	public String apply(Object o) {
		return "";
	}
}
