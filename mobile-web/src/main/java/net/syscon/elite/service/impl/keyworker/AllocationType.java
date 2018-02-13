package net.syscon.elite.service.impl.keyworker;

public enum AllocationType {
    AUTO("A"),
    MANUAL("M");

    private final String indicator;

    AllocationType(String indicator) {
        this.indicator = indicator;
    }

    public String getIndicator() {
        return indicator;
    }
}
