package net.syscon.elite.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import net.syscon.elite.aop.LoggingAspect;
import net.syscon.elite.aop.OracleConnectionAspect;

@Configuration
@EnableAspectJAutoProxy
public class AopConfigs {
	
	
	@Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
	
	@Bean
	public OracleConnectionAspect oracleProxyConnectionAspect() {
		return new OracleConnectionAspect();
	}
	

}
