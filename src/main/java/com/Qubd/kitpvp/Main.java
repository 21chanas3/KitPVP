package com.Qubd.kitpvp;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@SuppressWarnings("SpellCheckingInspection")
public class Main extends JavaPlugin {

    private DatabaseManager databaseManager;

    private Connection connection;
    public String url, username, password;

    Map<UUID, Combatant> combatantMap = new HashMap<UUID, Combatant>();

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("[KitPVP] Enabled!");
        getCommand("kit").setExecutor(new KitCommand());
        Bukkit.getPluginManager().registerEvents(new EventListeners(this),this);
        databaseManager = new DatabaseManager();
        mySQLSetup();
        getDatabaseManager().initializeTables();
        loadPreviousCombatants();
        config.addDefault("spawnx", -668.5);
        config.addDefault("spawny", 4.0);
        config.addDefault("spawnz", 602.5);
        config.addDefault("world", "GameLobby");
        config.options().copyDefaults(true);
        saveConfig();
    }
    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            Combatant currentCombatant = combatantMap.get(player.getUniqueId());
            getDatabaseManager().updatePlayer(player.getUniqueId(), currentCombatant.getKit(), currentCombatant.getLevel(), currentCombatant.getGold(), currentCombatant.getTotalExp(), currentCombatant.getKills(), currentCombatant.getDeaths());
            combatantMap.remove(player.getUniqueId());
        }
        if (combatantMap.isEmpty()) {
            System.out.println("Combatants stored!");
        } else {
            System.out.println("Combatant storage failed!");
        }
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
        Combatant combatant = combatantMap.get(player.getUniqueId());



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

        Score space2 = obj.getScore(" ");
        space2.setScore(6);

        Score expToNext = obj.getScore(ChatColor.GREEN.toString() +ChatColor.BOLD.toString() + "EXP Left");
        expToNext.setScore(5);

        double expToNextValue = 100 - (combatant.getTotalExp().doubleValue() % 100);
        //noinspection SpellCheckingInspection
        Team expToNextAmount = board.registerNewTeam("exptn");
        expToNextAmount.addEntry(ChatColor.GREEN.toString());
        expToNextAmount.setPrefix(expToNextValue + "");
        expToNextAmount.setSuffix("");
        obj.getScore(ChatColor.GREEN.toString()).setScore(4);

        Score space3 = obj.getScore("  ");
        space3.setScore(3);

        Score kdr = obj.getScore(ChatColor.AQUA.toString() + ChatColor.BOLD+ "KDR");
        kdr.setScore(2);

        Team kdrAmount = board.registerNewTeam("kdrAmount");
        kdrAmount.addEntry(ChatColor.AQUA.toString());
        kdrAmount.setPrefix(combatant.getKills() + "/" + combatant.getDeaths());
        obj.getScore(ChatColor.AQUA.toString()).setScore(1);

        player.setScoreboard(board);
    }

    public void mySQLSetup() {
         url = "jdbc:mysql://localhost:3306/luckperms?characterEncoding=utf8";
         username = "root";
         password = "ELrnLtI&SYQdUIeWsMNlc5W17Zd1uq@";

        try {
            synchronized (this) {
                if(getConnection() != null && !getConnection().isClosed()) {
                    return;
                }
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                setConnection(DriverManager.getConnection(this.url, this.username, this.password));
                Bukkit.getConsoleSender().sendMessage("SQL Database Connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void loadPreviousCombatants() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if (getDatabaseManager().playerExists(player.getUniqueId())) {
                ResultSet resultSet = getDatabaseManager().getPlayer(player.getUniqueId());
                try {
                    resultSet.next();
                    Combatant joinedCombatant = new Combatant(
                            player.getUniqueId(),
                            resultSet.getInt("kit"),
                            resultSet.getInt("level"),
                            resultSet.getInt("gold"),
                            resultSet.getBigDecimal("totalExp"),
                            resultSet.getInt("kills"),
                            resultSet.getInt("deaths"));
                    combatantMap.put(player.getUniqueId(),joinedCombatant);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            } else {
                getDatabaseManager().createPlayer(player.getUniqueId());
                Combatant joinedCombatant = new Combatant(player.getUniqueId(),1,1,50, BigDecimal.ZERO,0,0);
                combatantMap.put(player.getUniqueId(),joinedCombatant);
            }
            buildSidebar(player);
        }
        if (!combatantMap.isEmpty()) {
            System.out.println("Combatants loaded!");
        } else {
            System.out.println("Combatant loading failed!");
        }
    }

}
