package com.Qubd.kitpvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("SpellCheckingInspection")
public class Main extends JavaPlugin {

    Connection connection;
    private String host = "localhost";
    private String database = "luckperms";
    private String username = "root";
    private String password = "Sh1ngyan";
    private int port = 3306;

    Combatant[] onlineCombatants = new Combatant[50];
    Map<Player, Combatant> combatantMap = new HashMap<Player, Combatant>();

    @Override
    public void onEnable() {
        System.out.println("[KitPVP] Enabled!");
        getCommand("kit").setExecutor(new KitCommand());
        Bukkit.getPluginManager().registerEvents(new EventListeners(this),this);
        BukkitRunnable r = new BukkitRunnable() {
            public void run() {
                try {
                    openConnection();
                    Statement statement = connection.createStatement();
                    statement.executeUpdate("CREATE DATABASE IF NOT EXISTS kitpvp;");
                    statement.executeUpdate("USE kitpvp");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS playerData(uuid VARCHAR(36), kit INT, level INT, gold INT, totalEXP DECIMAL(10, 2), kills INT, deaths INT);");
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(this);

    }
    @Override
    public void onDisable() {

    }
    public static void applyKitUI(Player player) {
        //Creation
        Inventory kitGUI = Bukkit.createInventory(null,27, ChatColor.AQUA + "Kits");
        //Lores
        List<String> soliderLore = new ArrayList<String>();
        soliderLore.add(ChatColor.GRAY + "1x Chain Helmet");
        soliderLore.add(ChatColor.GRAY + "1x Chain Chestplate");
        soliderLore.add(ChatColor.GRAY + "1x Chain Leggings");
        soliderLore.add(ChatColor.GRAY + "1x Chain Boots");
        soliderLore.add(ChatColor.GRAY + "1x Iron Sword");
        soliderLore.add(ChatColor.GRAY + "4x Healing Soup");
        List<String> archerLore = new ArrayList<String>();
        archerLore.add(ChatColor.GRAY + "1x Leather Chestplate");
        archerLore.add(ChatColor.GRAY + "1x Leather Leggings");
        archerLore.add(ChatColor.GRAY + "1x Leather Boots");
        archerLore.add(ChatColor.GRAY + "1x Bow");
        archerLore.add(ChatColor.GRAY + "16x Arrow");
        archerLore.add(ChatColor.GRAY + "2x Healing Soup");
        List<String> ninjaLore = new ArrayList<String>();
        ninjaLore.add(ChatColor.GRAY + "1x Leather Boots");
        ninjaLore.add(ChatColor.GRAY + "1x Diamond Sword");
        ninjaLore.add(ChatColor.GRAY + "1x Healing Soup");
        ninjaLore.add(ChatColor.GRAY + "Permanent Speed 1");
        List<String> tankLore = new ArrayList<String>();
        tankLore.add(ChatColor.GRAY + "1x Iron Helmet");
        tankLore.add(ChatColor.GRAY + "1x Iron Chestplate");
        tankLore.add(ChatColor.GRAY + "1x Iron Leggings");
        tankLore.add(ChatColor.GRAY + "1x Iron Boots");
        tankLore.add(ChatColor.GRAY + "1x Wooden Sword");
        tankLore.add(ChatColor.GRAY + "8x Healing Soup");
        tankLore.add(ChatColor.GRAY + "Permanent Slowness 1");
        //ItemStacks
        ItemStack solider;
        solider = new ItemStack(Material.IRON_SWORD);
        ItemStack archer;
        archer = new ItemStack(Material.BOW);
        ItemStack ninja;
        ninja = new ItemStack(Material.LEATHER_BOOTS);
        ItemStack tank;
        tank = new ItemStack(Material.DIAMOND_CHESTPLATE);
        //Metadata
        ItemMeta soliderMeta = solider.getItemMeta();
        soliderMeta.setDisplayName("Solider");
        soliderMeta.setLore(soliderLore);
        solider.setItemMeta(soliderMeta);
        ItemMeta archerMeta = archer.getItemMeta();
        archerMeta.setDisplayName("Archer");
        archerMeta.setLore(archerLore);
        archer.setItemMeta(archerMeta);
        ItemMeta ninjaMeta = ninja.getItemMeta();
        ninjaMeta.setDisplayName("Ninja");
        ninjaMeta.setLore(ninjaLore);
        ninja.setItemMeta(ninjaMeta);
        ItemMeta tankMeta = tank.getItemMeta();
        tankMeta.setDisplayName("Tank");
        tankMeta.setLore(tankLore);
        tank.setItemMeta(tankMeta);
        //Item "Placement"
        kitGUI.setItem(10, solider);
        kitGUI.setItem(12, archer);
        kitGUI.setItem(14, ninja);
        kitGUI.setItem(16, tank);
        //Show GUI
        player.openInventory(kitGUI);
    }
    public void buildSidebar(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Combatant combatant = combatantMap.get(player);



        Objective obj = board.registerNewObjective("goldName", "dummy");
        obj.setDisplayName(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Mutinies");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score gold = obj.getScore(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "Gold");
        gold.setScore(11);

        Team goldAmount = board.registerNewTeam("gold_amount");
        goldAmount.addEntry(ChatColor.GOLD.toString());
        goldAmount.setPrefix(combatant.getGold() + "");
        obj.getScore(ChatColor.GOLD.toString()).setScore(10);

        Score space1 = obj.getScore("");
        space1.setScore(9);

        Score level = obj.getScore(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Level");
        level.setScore(8);

        Team levelAmount = board.registerNewTeam("level_amount");
        levelAmount.addEntry(ChatColor.DARK_GREEN.toString());
        levelAmount.setPrefix(combatant.getLevel() + "");
        levelAmount.setSuffix("");
        obj.getScore(ChatColor.DARK_GREEN.toString()).setScore(7);

        Score space2 = obj.getScore("");
        space2.setScore(6);

        Score expToNext = obj.getScore(ChatColor.GREEN.toString() +ChatColor.BOLD.toString() + "EXP Left");
        expToNext.setScore(5);

        double expToNextValue = combatant.getTotalExp().doubleValue() % 100;
        //noinspection SpellCheckingInspection
        Team expToNextAmount = board.registerNewTeam("exptn");
        expToNextAmount.addEntry(ChatColor.GREEN.toString());
        expToNextAmount.setPrefix(expToNextValue + "");
        expToNextAmount.setSuffix("");
        obj.getScore(ChatColor.GREEN.toString()).setScore(4);

        Score space3 = obj.getScore("");
        space3.setScore(3);

        Score kdr = obj.getScore(ChatColor.AQUA.toString() + ChatColor.BOLD+ "KDR");
        kdr.setScore(2);

        Team kdrAmount = board.registerNewTeam("kdrAmount");
        kdrAmount.addEntry(ChatColor.AQUA.toString());
        kdrAmount.setPrefix(combatant.getKills() + "/" + combatant.getDeaths());
        obj.getScore(ChatColor.AQUA.toString()).setScore(1);

        player.setScoreboard(board);
    }
    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}
