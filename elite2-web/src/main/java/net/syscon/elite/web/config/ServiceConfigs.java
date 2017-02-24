package net.syscon.elite.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PersistenceConfigs.class)
@ComponentScan(basePackages = {"net.syscon.elite.service"})
public class ServiceConfigs {

//	@Bean
//	public AgencyLocationService agencyService() {
//		return new AgencyLocationServiceImpl();
//	}

	
	
}
