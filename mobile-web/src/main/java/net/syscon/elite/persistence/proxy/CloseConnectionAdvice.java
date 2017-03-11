package net.syscon.elite.persistence.proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

class CloseConnectionAdvice implements MethodInterceptor {

  @Override
public Object invoke(final MethodInvocation invocation) throws Throwable {
    System.out.println("CloseConnectionAdvice.Invoking " + invocation.getMethod().getName());
    System.out.println(invocation.getThis());
    final Object retVal = invocation.proceed();
    System.out.println("CloseConnectionAdvice.Done");
    return retVal;
  }
}
