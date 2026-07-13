package com.osuserverlist.bjar.models.direct;

import java.util.List;

import lombok.Data;

@Data
public class DirectBeatmapSet {
    private String Artist;
    private String Title;
    private String Creator;
    private int RankedStatus;
    private String LastUpdate;
    private int SetID;
    private Object HasVideo;
    private List<DirectBeatmap> ChildrenBeatmaps;
}
