package net.syscon.elite.web;


import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.web.WebAppConfiguration;

import net.syscon.elite.web.config.ApplicationContextConfigs;
import net.syscon.elite.web.config.WebSecurityConfig;

@WebAppConfiguration
@ContextHierarchy ({
    @ContextConfiguration (classes = ApplicationContextConfigs.class),
    @ContextConfiguration(classes = WebSecurityConfig.class)
})
public class MobileApiServerIntgTest {
	
	

}
