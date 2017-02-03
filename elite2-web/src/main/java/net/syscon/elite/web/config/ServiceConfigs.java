package net.syscon.elite.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.impl.AgencyServiceImpl;

@Configuration
@Import(PersistenceConfigs.class)
@ComponentScan(basePackages = {"net.syscon.elite.web.api"})
public class ServiceConfigs {

	@Bean
	public AgencyService agencyService() {
		return new AgencyServiceImpl();
	}

	
	
}
