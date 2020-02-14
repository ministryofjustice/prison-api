package net.syscon.elite.repository.jpa.model;

public enum GroupLeaderFlag {
    Y(true),
    N(false);

    private boolean groupLeader;

    GroupLeaderFlag(boolean groupLeader) {
        this.groupLeader = groupLeader;
    }

    public boolean isGroupLeader() {
        return groupLeader;
    }
}
