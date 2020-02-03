package net.syscon.elite.repository.jpa.model;

public enum ActiveFlag {
    Y(true),
    N(false);

    private boolean active;

    ActiveFlag(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
