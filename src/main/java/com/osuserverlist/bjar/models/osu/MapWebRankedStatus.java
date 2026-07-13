package com.osuserverlist.bjar.models.osu;

import me.skiincraft.api.ousu.entity.objects.Approval;

public enum MapWebRankedStatus {
    NOT_SUBMITTED(-1),
    PENDING(0),
    UPDATE_AVAILABLE(1),
    RANKED(2),
    APPROVED(3),
    QUALIFIED(4),
    LOVED(5);

    private final int id;

    MapWebRankedStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MapWebRankedStatus fromId(int id) {
        for (MapWebRankedStatus status : values()) {
            if (status.getId() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ranked status ID: " + id);
    }

    public static MapWebRankedStatus fromApproval(Approval approval) {
        switch(approval) {
            case Pending:
                return PENDING;
            case Ranked:
                return RANKED;
            case Approved:
                return APPROVED;
            case Qualified:
                return QUALIFIED;
            case Loved:
                return LOVED;
            default:
                return NOT_SUBMITTED;
        }
    }
}
