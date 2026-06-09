package com.osuserverlist.bjar.models.osu;

public enum ActionStatus {
    EDITING(3),
    WATCHING(6),
    TESTING(8),
    SUBMITTING(9);

    private final int id;

    private ActionStatus(int id) 
    {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static ActionStatus getById(int id) {
        return (ActionStatus) java.util.Arrays.stream(values())
            .filter(s -> s.id == id)
            .findFirst()
            .orElse(null);
    }
}
