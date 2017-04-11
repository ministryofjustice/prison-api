package net.syscon.elite.web.integration.test;

import net.syscon.elite.web.api.resource.imp.AgenciesResourceImplIntegTest;
import net.syscon.elite.web.api.resource.imp.UsersResourceImplIntegTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	UsersResourceImplIntegTest.class,
	AgenciesResourceImplIntegTest.class
})
public class IntegTestSuite {

}
