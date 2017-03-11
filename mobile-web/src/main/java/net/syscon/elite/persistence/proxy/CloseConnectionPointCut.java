package net.syscon.elite.persistence.proxy;

import java.lang.reflect.Method;

import org.springframework.aop.support.StaticMethodMatcherPointcut;

public class CloseConnectionPointCut extends StaticMethodMatcherPointcut  {

	@Override
	public boolean matches(final Method method, final Class<?> targetClass) {
		final String targetMethod = "close";
		boolean intercept = false;
		if (targetMethod.equals(method.getName())) {
			intercept = true;
		}
		return intercept;
	}
	
}