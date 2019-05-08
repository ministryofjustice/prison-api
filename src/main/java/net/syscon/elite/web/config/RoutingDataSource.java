package net.syscon.elite.web.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<Route> ctx = new ThreadLocal<>();

    public enum Route {
        PRIMARY, REPLICA
    }

    public static void clearReplicaRoute() {
        ctx.remove();
    }

    public static void setPrimaryRoute() {
        ctx.set(Route.PRIMARY);
    }


    @Override
    protected Object determineCurrentLookupKey() {
        return ctx.get();
    }
}
