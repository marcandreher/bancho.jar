package com.osuserverlist.bjar.models.osu.replay;

import java.util.List;

import lombok.Data;

@Data
public class ReplayFrameBundle {
    private List<ReplayFrame> frames;
    private ScoreFrame scoreFrame;
    private ReplayAction action;

    private int extra;
    private int sequence;

    private byte[] rawData;
}
