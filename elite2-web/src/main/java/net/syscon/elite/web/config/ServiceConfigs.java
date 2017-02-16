package net.syscon.elite.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import net.syscon.elite.service.AgencyLocationService;
import net.syscon.elite.service.impl.AgencyLocationServiceImpl;

@Configuration
@Import(PersistenceConfigs.class)
@ComponentScan(basePackages = {"net.syscon.elite.web.api"})
public class ServiceConfigs {

	@Bean
	public AgencyLocationService agencyService() {
		return new AgencyLocationServiceImpl();
	}

	
	
}
