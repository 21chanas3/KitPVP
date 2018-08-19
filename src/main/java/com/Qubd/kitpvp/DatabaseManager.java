package com.Qubd.kitpvp;

import com.avaje.ebean.RawSql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {
    private Main plugin = Main.getPlugin(Main.class);

    public boolean playerExists(UUID uuid) {
        try {
            PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM players WHERE uuid =?");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public ResultSet getPlayer(UUID uuid) {
        try {
            PreparedStatement getPlayerStatement = plugin.getConnection().prepareStatement("SELECT * FROM players WHERE uuid = ?");
            getPlayerStatement.setString(1,uuid.toString());

            return getPlayerStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void createPlayer(UUID uuid) {
        try {
            PreparedStatement createPlayer = plugin.getConnection().prepareStatement("INSERT INTO players VALUES (?,?,?,?,?,?,?)");
            createPlayer.setString(1, uuid.toString());
            createPlayer.setInt(2, 1);
            createPlayer.setInt(3, 1);
            createPlayer.setInt(4, 50);
            createPlayer.setBigDecimal(5, BigDecimal.ZERO);
            createPlayer.setInt(6,0);
            createPlayer.setInt(7,0);
            createPlayer.executeUpdate();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updatePlayer(final UUID uuid, int kit, int level, int gold, BigDecimal totalExp, int kills, int deaths) {
        try {
            PreparedStatement updatePlayer = plugin.getConnection().prepareStatement("UPDATE players SET uuid = ?,kit = ?,level = ?, gold = ?,totalExp = ?,kills = ?,deaths = ? WHERE uuid = ?");
            updatePlayer.setString(1, uuid.toString());
            updatePlayer.setInt(2, kit);
            updatePlayer.setInt(3, level);
            updatePlayer.setInt(4, gold);
            updatePlayer.setBigDecimal(5, totalExp);
            updatePlayer.setInt(6,kills);
            updatePlayer.setInt(7,deaths);
            updatePlayer.setString(8, uuid.toString());
            updatePlayer.executeUpdate();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
