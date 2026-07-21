package com.osuserverlist.bjar.models.database;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "stats")
public class StatsEntity {

    @EmbeddedId
    private StatsId id;

    @Column(name = "tscore", nullable = false)
    private Long totalScore = 0L;

    @Column(name = "rscore", nullable = false)
    private Long rankedScore = 0L;

    @Column(name = "pp", nullable = false)
    private Integer pp = 0;

    @Column(name = "plays", nullable = false)
    private Integer plays = 0;

    @Column(name = "playtime", nullable = false)
    private Integer playtime = 0;

    @Column(name = "acc", nullable = false)
    private Float accuracy = 0f;

    @Column(name = "max_combo", nullable = false)
    private Integer maxCombo = 0;

    @Column(name = "total_hits", nullable = false)
    private Integer totalHits = 0;

    @Column(name = "replay_views", nullable = false)
    private Integer replayViews = 0;

    @Column(name = "xh_count", nullable = false)
    private Integer xhCount = 0;

    @Column(name = "x_count", nullable = false)
    private Integer xCount = 0;

    @Column(name = "sh_count", nullable = false)
    private Integer shCount = 0;

    @Column(name = "s_count", nullable = false)
    private Integer sCount = 0;

    @Column(name = "a_count", nullable = false)
    private Integer aCount = 0;
}