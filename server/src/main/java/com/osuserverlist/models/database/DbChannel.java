package com.osuserverlist.models.database;

import de.marcandreher.fusionkit.core.database.Column;
import lombok.Data;

@Data
public class DbChannel {
    @Column("id")
    private int id;

    @Column("name")
    private String name;

    @Column("topic")
    private String topic;

    @Column("read_priv")
    private int readPriv;

    @Column("write_priv")
    private int writePriv;

    @Column("auto_join")
    private boolean autoJoin;
}
