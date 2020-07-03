package uk.gov.justice.hmpps.prison.repository.jpa.model;

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
