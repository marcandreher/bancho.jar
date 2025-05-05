package com.banchojar;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.javalin.http.Context;

public class HandleItYourself {
    private static void handleOsuDirect(Context ctx) throws IOException {
        String user = ctx.queryParam("u");
        String pass = ctx.queryParam("h");

    
        // Basic auth check placeholder
        if (user != null) {
            return;
        }
    
        // Simulated beatmap sets (2 sets)
        List<Map<String, Object>> beatmapSets = List.of(
            Map.of(
                "Artist", "Camellia",
                "Title", "Exit This Earth's Atomosphere",
                "Creator", "Monstrata",
                "RankedStatus", 1,
                "LastUpdate", "2023-01-20 14:22:00",
                "SetID", 111111,
                "HasVideo", 1,
                "Diffs", List.of(
                    Map.of("DifficultyRating", 5.3, "DiffName", "Extra", "CS", 4.0, "OD", 8.5, "AR", 9.2, "HP", 6.5, "Mode", 0),
                    Map.of("DifficultyRating", 4.0, "DiffName", "Hard", "CS", 4.0, "OD", 7.0, "AR", 8.0, "HP", 5.0, "Mode", 0)
                )
            ),
            Map.of(
                "Artist", "xi",
                "Title", "FREEDOM DiVE",
                "Creator", "Nakagawa-Kanon",
                "RankedStatus", 1,
                "LastUpdate", "2022-05-15 09:00:00",
                "SetID", 222222,
                "HasVideo", 0,
                "Diffs", List.of(
                    Map.of("DifficultyRating", 5.7, "DiffName", "Freedom", "CS", 4.0, "OD", 8.0, "AR", 9.5, "HP", 6.0, "Mode", 0)
                )
            )
        );
    
        StringBuilder out = new StringBuilder();
        out.append(beatmapSets.size()).append("\n");
    
        for (Map<String, Object> set : beatmapSets) {
            List<Map<String, Object>> diffs = (List<Map<String, Object>>) set.get("Diffs");
    
            String diffsStr = diffs.stream().map(diff -> String.format(
                "[%.2f⭐] %s {cs: %.1f / od: %.1f / ar: %.1f / hp: %.1f}@%d",
                ((Number) diff.get("DifficultyRating")).doubleValue(),
                OsuHandler.sanitize((String) diff.get("DiffName")),
                ((Number) diff.get("CS")).doubleValue(),
                ((Number) diff.get("OD")).doubleValue(),
                ((Number) diff.get("AR")).doubleValue(),
                ((Number) diff.get("HP")).doubleValue(),
                ((Number) diff.get("Mode")).intValue()
            )).reduce((a, b) -> a + "," + b).orElse("");
    
            out.append(String.format(
                "%d.osz|%s|%s|%s|%d|10.0|%s|%d|0|%d|0|0|0|%s\n",
                ((Number) set.get("SetID")).intValue(),
                OsuHandler.sanitize((String) set.get("Artist")),
                OsuHandler.sanitize((String) set.get("Title")),
                set.get("Creator"),
                ((Number) set.get("RankedStatus")).intValue(),
                set.get("LastUpdate"),
                ((Number) set.get("SetID")).intValue(),
                ((Number) set.get("HasVideo")).intValue(),
                diffsStr
            ));
        }
    
        ctx.result(out.toString());
    }
}
