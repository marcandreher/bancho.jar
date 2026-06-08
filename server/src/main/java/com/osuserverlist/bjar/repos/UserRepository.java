package com.osuserverlist.bjar.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.modules.database.MySQL;

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
    
    private final static String REMOVE_FRIEND_QUERY = "DELETE FROM `relationships` WHERE (`user1` = ? AND `user2` = ?) OR (`user1` = ? AND `user2` = ?)";
    private final static String ADD_FRIEND_QUERY = "INSERT INTO `relationships` (`user1`, `user2`) VALUES (?, ?)";
    private final static String GET_FRIEND_IDS_QUERY = "SELECT * FROM `relationships` WHERE `user1` = ?";
    private final static String INSERT_STATS_QUERY = "INSERT INTO `stats`(`id`, `mode`) VALUES (?,?)";
    private final static String INSERT_USER_QUERY = "INSERT INTO `users`(`name`, `safe_name`, `email`, `pw_bcrypt`, `creation_time`) VALUES (?, ?, ?, ?, ?)";
    private final static String GET_USER_BY_NAME_OR_EMAIL_QUERY = "SELECT * FROM `users` WHERE `name` = ? OR `email` = ?";
}
