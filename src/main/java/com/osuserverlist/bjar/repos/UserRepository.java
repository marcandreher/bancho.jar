package com.osuserverlist.bjar.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.modules.datastore.MySQL;

public class UserRepository {

    private final MySQL mysql;

    public UserRepository(MySQL mysql) {
        this.mysql = mysql;
    }

    public UserEntity getUserByNameOrMail(String name, String email) throws SQLException {
        ResultSet userResult = mysql.query(GET_USER_BY_NAME_OR_EMAIL_QUERY, name, email).executeQuery();

        if (!userResult.next()) {
            return null;
        }

        return UserEntity.fromResultSet(userResult);
    }

    public UserEntity getUserByName(String name) throws SQLException {
        ResultSet userResult = mysql.query(GET_USER_BY_NAME_QUERY, name).executeQuery();

        if (!userResult.next()) {
            return null;
        }

        return UserEntity.fromResultSet(userResult);
    }

    public List<Integer> getFriendIds(int userId) throws SQLException {
        List<Integer> friendIds = new ArrayList<>();
        
        ResultSet friendResult = mysql.query(GET_FRIEND_IDS_QUERY, userId).executeQuery();
        while (friendResult.next()) {
            friendIds.add(friendResult.getInt("user2"));
        }
        
        return friendIds;
    }

    public void addFriend(int userId, int friendId) throws SQLException {
        mysql.exec(ADD_FRIEND_QUERY, userId, friendId);
    }

    public void removeFriend(int userId, int friendId) throws SQLException {
        mysql.exec(REMOVE_FRIEND_QUERY, userId, friendId, friendId, userId);
    }

    public void insertStats(int userId, int mode) throws SQLException {
        mysql.exec(INSERT_STATS_QUERY, userId, mode);
    }

    public void insertUser(String name, String safeName, String email, String pwBcrypt) throws SQLException {
        int creationTime = (int) (System.currentTimeMillis() / 1000);
        mysql.exec(INSERT_USER_QUERY, name, safeName, email, pwBcrypt, creationTime);
    }

    public void updateUserPrivileges(int userId, int privileges) throws SQLException {
        mysql.exec(UPDATE_USER_PRIVS_QUERY, privileges, userId);
    }

    public void insertIngameLogin(int userId, String ip, String osuVer, String osuStream) throws SQLException {
        mysql.exec(INSERT_INGAME_LOGIN_QUERY, userId, ip, osuVer, osuStream);
    }

    public void updateUserCountry(int userId, String country) throws SQLException {
        mysql.exec(UPDATE_USER_COUNTRY_QUERY, country, userId);
    }

    private final static String REMOVE_FRIEND_QUERY = "DELETE FROM `relationships` WHERE (`user1` = ? AND `user2` = ?) OR (`user1` = ? AND `user2` = ?)";
    private final static String ADD_FRIEND_QUERY = "INSERT INTO `relationships` (`user1`, `user2`) VALUES (?, ?)";
    private final static String GET_FRIEND_IDS_QUERY = "SELECT * FROM `relationships` WHERE `user1` = ?";
    private final static String UPDATE_USER_COUNTRY_QUERY = "UPDATE `users` SET `country` = ? WHERE `id` = ?";
    private final static String UPDATE_USER_PRIVS_QUERY = "UPDATE `users` SET `priv` = ? WHERE `id` = ?";
    private final static String INSERT_STATS_QUERY = "INSERT INTO `stats`(`id`, `mode`) VALUES (?,?)";
    private final static String INSERT_USER_QUERY = "INSERT INTO `users`(`name`, `safe_name`, `email`, `pw_bcrypt`, `creation_time`) VALUES (?, ?, ?, ?, ?)";
    private final static String INSERT_INGAME_LOGIN_QUERY = "INSERT INTO `ingame_logins` (`userid`, `ip`, `osu_ver`, `osu_stream`, `datetime`) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    private final static String GET_USER_BY_NAME_OR_EMAIL_QUERY = "SELECT * FROM `users` WHERE `name` = ? OR `email` = ?";
    private final static String GET_USER_BY_NAME_QUERY = "SELECT * FROM `users` WHERE `name` = ?";
}
