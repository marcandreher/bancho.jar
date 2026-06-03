package com.osuserverlist.bjar.models.osu;

import java.util.Base64;
import java.util.Base64.Decoder;

import io.javalin.http.Context;
import lombok.Data;

@Data
public class SubmitResponse {
    private String token;
    private boolean exitedOut;
    private int failTime;
    private String updatedBeatmapHash;
    private String storyboardMd5;
    private String uniqueIds;
    private int scoreTime;
    private String osuVersion;
    private byte[] visualSettings;
    private byte[] iv;
    private byte[] clientHash;
    private byte[] scoreEncrypted;

    public static SubmitResponse fromContext(Context ctx) {
        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setToken(ctx.formParam("token"));
        submitResponse.setExitedOut(Boolean.parseBoolean(ctx.formParam("x")));
        submitResponse.setFailTime(Integer.parseInt(ctx.formParam("ft")));
        submitResponse.setUpdatedBeatmapHash(ctx.formParam("bmk"));
        submitResponse.setStoryboardMd5(ctx.formParam("sbk"));
        submitResponse.setUniqueIds(ctx.formParam("c1"));
        submitResponse.setScoreTime(Integer.parseInt(ctx.formParam("st")));
        submitResponse.setOsuVersion(ctx.formParam("osuver"));

        Decoder decoder = Base64.getDecoder();
        submitResponse.setVisualSettings(decoder.decode(ctx.formParam("fs")));
        submitResponse.setIv(decoder.decode(ctx.formParam("iv")));
        submitResponse.setClientHash(decoder.decode(ctx.formParam("s")));
        submitResponse.setScoreEncrypted(decoder.decode(ctx.formParam("score")));

        return submitResponse;
    }
}
