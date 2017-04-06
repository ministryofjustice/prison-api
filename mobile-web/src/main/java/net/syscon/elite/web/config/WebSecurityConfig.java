package net.syscon.elite.web.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

import net.syscon.elite.security.jwt.JWTAuthenticationFilter;
import net.syscon.elite.security.jwt.JWTLoginFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	
	@Value("spring.datasource.hikari.driver-class-name")
	private String jdbcDriver;
	
	@Value("spring.datasource.hikari.jdbc-url")
    private String jdbcUrl;
	

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.authorizeRequests().anyRequest().authenticated().and().formLogin().loginProcessingUrl("/api/auth/login").usernameParameter("username").passwordParameter("password").and().csrf().disable()
				// We filter the api/login requests
				.addFilterBefore(new JWTLoginFilter("/api/auth/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
				// And filter other requests to check the presence of JWT in
				// header
				.addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		// Create a default account
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider() {
		return new CustomAuthenticationProvider();
	}
	
	

	@Configurable
	@Service
	public static class CustomAuthenticationProvider implements AuthenticationProvider {

		@Override
		public Authentication authenticate(final Authentication auth) throws AuthenticationException {
	        
			
			
			final String username = auth.getName();
	        final String password = auth.getCredentials().toString();
	        

	        
	        // to add more logic
	        final List<GrantedAuthority> grantedAuths = new ArrayList<>();
	        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
	        return new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
	        
	        
	        
	        
		}

		@Override
		public boolean supports(final Class<?> authentication) {
			return true;
		}

	}

}