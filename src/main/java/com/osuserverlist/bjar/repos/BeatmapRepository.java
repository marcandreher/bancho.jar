package com.osuserverlist.bjar.repos;

import java.util.List;

import com.osuserverlist.bjar.models.database.BeatmapEntity;

import io.ebean.DB;

public class BeatmapRepository {
    public static BeatmapEntity findById(long id) {
        return DB.find(BeatmapEntity.class, id);
    }

    public static BeatmapEntity findByMd5(String md5) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("md5", md5)
                .findOne();
    }

    public static BeatmapEntity findByFilename(String filename) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("filename", filename)
                .findOne();
    }

    public static List<BeatmapEntity> findBySetId(long setId) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("setId", setId)
                .findList();
    }

    public static BeatmapEntity findFirstBySetId(long setId) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("setId", setId)
                .setMaxRows(1)
                .findOne();
    }

    public static List<BeatmapEntity> findByStatus(int status) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("status", status)
                .findList();
    }

    public static List<BeatmapEntity> findByMode(int mode) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("mode", mode)
                .findList();
    }

    public static boolean exists(long id) {
        return DB.find(BeatmapEntity.class)
                .where()
                .idEq(id)
                .exists();
    }

    public static boolean existsMd5(String md5) {
        return DB.find(BeatmapEntity.class)
                .where()
                .eq("md5", md5)
                .exists();
    }

    public static void save(BeatmapEntity beatmap) {
        DB.save(beatmap);
    }

    public static void update(BeatmapEntity beatmap) {
        DB.update(beatmap);
    }

    public static void delete(BeatmapEntity beatmap) {
        DB.delete(beatmap);
    }

    public static void deleteById(long id) {
        DB.delete(BeatmapEntity.class, id);
    }

    public static void incrementPlays(long id) {
        DB.sqlUpdate("""
                UPDATE maps
                SET plays = plays + 1
                WHERE id = ?
                """)
                .setParameter(1, id)
                .execute();
    }

    public static void incrementPasses(long id) {
        DB.sqlUpdate("""
                UPDATE maps
                SET passes = passes + 1
                WHERE id = ?
                """)
                .setParameter(1, id)
                .execute();
    }

    public static void incrementPlayAndPass(long id) {
        DB.sqlUpdate("""
                UPDATE maps
                SET plays = plays + 1,
                    passes = passes + 1
                WHERE id = ?
                """)
                .setParameter(1, id)
                .execute();
    }

    public static List<BeatmapEntity> findAll() {
        return DB.find(BeatmapEntity.class)
                .findList();
    }

    public static int updateStatusById(long id, int status, boolean frozen) {
        return DB.sqlUpdate("""
                UPDATE maps
                SET status = :status,
                    frozen = :frozen
                WHERE id = :id
                """)
                .setParameter("status", status)
                .setParameter("frozen", frozen)
                .setParameter("id", id)
                .execute();
    }

    public static int updateStatusBySetId(long setId, int status, boolean frozen) {
        return DB.sqlUpdate("""
                UPDATE maps
                SET status = :status,
                    frozen = :frozen
                WHERE set_id = :setId
                """)
                .setParameter("status", status)
                .setParameter("frozen", frozen)
                .setParameter("setId", setId)
                .execute();
    }

    public static long count() {
        return DB.find(BeatmapEntity.class)
                .findCount();
    }
}
