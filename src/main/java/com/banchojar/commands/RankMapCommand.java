package com.banchojar.commands;

import org.jooq.impl.DSL;

import com.banchojar.Player;
import com.banchojar.Server;

public class RankMapCommand extends AbstractBanchoCommandHandler {

    @Override
    public String commandName() {
        return "!rankmap";
    }

    @Override
    public String description() {
        return "Get the rank of a map.";
    }

    @Override
    public String handle(Player player, String[] args) {
        int beatmapId = player.getLastNpBeatmapId();

        if(beatmapId == 0) {
            return "No beatmap ID found. Please /np a beatmap first.";
        }

        Server.dsl.update(DSL.table("beatmaps"))
            .set(DSL.field("status"), 1)
            .where(DSL.field("beatmap_id").eq(beatmapId))
            .execute();
        return "Beatmap (" + beatmapId + ") status updated to ranked.";
    }
    
}
