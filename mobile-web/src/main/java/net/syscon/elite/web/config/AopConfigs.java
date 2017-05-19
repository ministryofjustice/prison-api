package net.syscon.elite.web.config;

import net.syscon.elite.aop.LoggingAspect;
import net.syscon.elite.aop.OracleConnectionAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAspectJAutoProxy
public class AopConfigs {
	
	
	@Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
	
	@Bean
	@Profile("!noproxy")
	public OracleConnectionAspect oracleProxyConnectionAspect() {
		return new OracleConnectionAspect();
	}
	

}
