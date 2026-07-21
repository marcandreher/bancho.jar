package com.osuserverlist.bjar.repos;

import java.util.List;

import com.osuserverlist.bjar.models.database.ChannelEntity;

import io.ebean.DB;

public class ChannelRepository {
    public static ChannelEntity findById(int id) {
        return DB.find(ChannelEntity.class, id);
    }

    public static ChannelEntity findByName(String name) {
        return DB.find(ChannelEntity.class)
                .where()
                .eq("name", name)
                .findOne();
    }

    public static List<ChannelEntity> findAll() {
        return DB.find(ChannelEntity.class)
                .orderBy("name")
                .findList();
    }

    public static List<ChannelEntity> findAutoJoinChannels() {
        return DB.find(ChannelEntity.class)
                .where()
                .eq("autoJoin", true)
                .orderBy("name")
                .findList();
    }

    public static void save(ChannelEntity channel) {
        DB.save(channel);
    }

    public static void delete(ChannelEntity channel) {
        DB.delete(channel);
    }

    public static boolean exists(String name) {
        return DB.find(ChannelEntity.class)
                .where()
                .eq("name", name)
                .exists();
    }

    public static long count() {
        return DB.find(ChannelEntity.class)
                .findCount();
    }

    public static ChannelEntity create(String name,
                                       String topic,
                                       int readPriv,
                                       int writePriv,
                                       boolean autoJoin) {

        ChannelEntity channel = new ChannelEntity();

        channel.setName(name);
        channel.setTopic(topic);
        channel.setReadPriv(readPriv);
        channel.setWritePriv(writePriv);
        channel.setAutoJoin(autoJoin);

        DB.save(channel);

        return channel;
    }
}
