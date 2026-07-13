package com.osuserverlist.bjar.models.osu.replay;

public enum ReplayAction {
    Standard(0),
    NewSong(1),
    Skip(2),
    Completion(3),
    Fail(4),
    Pause(5),
    Unpause(6),
    SongSelect(7),
    WatchingOther(8);

    private final int id;

    private ReplayAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ReplayAction fromId(int id) {
        for (ReplayAction action : values()) {
            if (action.id == id) {
                return action;
            }
        }
        throw new IllegalArgumentException("Invalid ReplayAction id: " + id);
    }
}
