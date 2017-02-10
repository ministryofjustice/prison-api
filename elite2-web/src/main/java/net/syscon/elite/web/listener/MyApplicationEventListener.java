package net.syscon.elite.web.listener;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

public class MyApplicationEventListener implements ApplicationEventListener {
	@Override
    public void onEvent(ApplicationEvent applicationEvent) {
        switch (applicationEvent.getType()) {
            case INITIALIZATION_FINISHED:
                System.out.println("Jersey application started.");
                break;
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new MyRequestEventListener();
    }


    public static class MyRequestEventListener implements RequestEventListener {
        private volatile long methodStartTime;

        @Override
        public void onEvent(RequestEvent requestEvent) {
            switch (requestEvent.getType()) {
                case RESOURCE_METHOD_START:
                    methodStartTime = System.currentTimeMillis();
                    break;
                case RESOURCE_METHOD_FINISHED:
                    long methodExecution = System.currentTimeMillis() - methodStartTime;
                    final String methodName = requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable().getHandlingMethod().getName();
                    System.out.println("Method '" + methodName + "' executed. Processing time: " + methodExecution + " ms");
                    break;
            }
        }
    }
}