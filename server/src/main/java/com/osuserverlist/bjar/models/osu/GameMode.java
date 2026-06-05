package com.osuserverlist.bjar.models.osu;

public enum GameMode {
    VANILLA_OSU(0),
    VANILLA_TAIKO(1),
    VANILLA_CATCH(2),
    VANILLA_MANIA(3),

    RELAX_OSU(4),
    RELAX_TAIKO(5),
    RELAX_CATCH(6),
    RELAX_MANIA(7),

    AUTOPILOT_OSU(8),
    AUTOPILOT_TAIKO(9),
    AUTOPILOT_CATCH(10),
    AUTOPILOT_MANIA(11); 

    private final int value;

    GameMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GameMode fromValue(int modeVn, int mods) {
        int mode = modeVn;

        if ((mods & Mods.Relax2.getValue()) != 0) {
            mode += 8;
        } else if ((mods & Mods.Relax.getValue()) != 0) {
            mode += 4;
        }

        for (GameMode gm : GameMode.values()) {
            if (gm.getValue() == mode) {
                return gm;
            }
        }

        throw new IllegalArgumentException("Invalid game mode value: " + mode);
    }


}
