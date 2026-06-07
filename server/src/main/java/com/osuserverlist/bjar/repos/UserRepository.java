package com.osuserverlist.bjar.repos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.osuserverlist.bjar.modules.database.MySQL;

public class UserRepository {

    private final MySQL mysql;

    public UserRepository(MySQL mysql) {
        this.mysql = mysql;
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
    
    private final static String REMOVE_FRIEND_QUERY = "DELETE FROM `relationships` WHERE (`user1` = ? AND `user2` = ?) OR (`user1` = ? AND `user2` = ?)";
    private final static String ADD_FRIEND_QUERY = "INSERT INTO `relationships` (`user1`, `user2`) VALUES (?, ?)";
    private final static String GET_FRIEND_IDS_QUERY = "SELECT * FROM `relationships` WHERE `user1` = ?";

}
