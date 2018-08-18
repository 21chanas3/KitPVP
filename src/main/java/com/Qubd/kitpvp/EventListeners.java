package com.Qubd.kitpvp;



import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class EventListeners implements Listener {
    private Main main;

    public EventListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = e.getEntity().getKiller();
        e.setDeathMessage(killer.getDisplayName() + " killed " + victim.getDisplayName());
        Combatant killedCombatant = main.combatantMap.get(victim);
        Combatant killerCombatant = main.combatantMap.get(killer);
        killedCombatant.setDeaths(killedCombatant.getDeaths()+1);
        killerCombatant.setKills(killerCombatant.getKills()+1);
        double difference = killedCombatant.getTotalExp().doubleValue() - killerCombatant.getTotalExp().doubleValue();
        double multiplier = (0.002 * difference) + 1;
        int goldToAdd = (int) Math.round(5 * multiplier);
        killerCombatant.setGold(killedCombatant.getGold()+goldToAdd);
        double oldTotalExp = killedCombatant.getTotalExp().doubleValue();
        double newTotalExp = oldTotalExp + (multiplier * 10);
        BigDecimal convertedTotalExp  = new BigDecimal(newTotalExp);
        killerCombatant.setTotalExp(convertedTotalExp);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws IOException {
        final Player player = e.getPlayer();
        if(player.hasPlayedBefore()) {
            BukkitRunnable r = new BukkitRunnable() {
                public void run() {
                    try {
                        main.openConnection();
                        Statement statement = main.connection.createStatement();
                        statement.executeUpdate("USE kitpvp");
                        ResultSet result = statement.executeQuery("SELECT * FROM playerData WHERE uuid = " + player.getUniqueId());
                        Combatant joiningCombatant = new Combatant(
                                result.getInt("kit"),
                                result.getInt("level"),
                                result.getInt("gold"),
                                result.getBigDecimal("totalExp"),
                                result.getInt("kills"),
                                result.getInt("deaths"));
                        main.combatantMap.put(player, joiningCombatant);
                    } catch(ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }
                }
            };
            r.runTaskAsynchronously(main);
        } else {
            BigDecimal totalExp = new BigDecimal("0");
            Combatant defaultCombatant = new Combatant(1, 0, 100, totalExp, 0, 0);
            main.combatantMap.put(player, defaultCombatant);
        }
        main.buildSidebar(player);
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        Combatant leavingCombatant = main.combatantMap.get(player);
        final int kit = leavingCombatant.getKit();
        final int level = leavingCombatant.getLevel();
        final int gold = leavingCombatant.getGold();
        final int deaths = leavingCombatant.getDeaths();
        final int kills = leavingCombatant.getKills();
        final BigDecimal totalExp = leavingCombatant.getTotalExp();
        BukkitRunnable r = new BukkitRunnable() {
            public void run() {
                try {
                    main.openConnection();
                    Statement statement = main.connection.createStatement();
                    statement.executeUpdate("USE kitpvp");
                    ResultSet result = statement.executeQuery("SELECT * FROM playerData WHERE uuid = " + player.getUniqueId());
                    if (result.next()) {
                        statement.executeUpdate("UPDATE kitpvp SET kit ='"+kit+"', level ='"+level+ "', gold ='"+gold+ "', deaths ='"+deaths+ "', kills ='"+kills+ "', totalExp ='"+totalExp+"' WHERE uuid ="+ player.getUniqueId()+";");
                    } else {
                        statement.executeUpdate("INSERT INTO kitpvp VALUES(" + player.getUniqueId() +"," + kit +","+ level +","+ gold +","+ totalExp +","+ kills +","+ deaths +");");
                    }
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        r.runTaskAsynchronously(main);
        main.combatantMap.remove(player);
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Combatant choosingCombatant = main.combatantMap.get(player);

        if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle()).equals(ChatColor.AQUA + "Kits")) {
            if (e.getCurrentItem() != null) {
                e.setCancelled(true);
                switch (e.getCurrentItem().getType()) {
                    case IRON_SWORD:
                        for(PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                        player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                        player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                        player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                        player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD, 1));
                        player.getInventory().setItem(1, new ItemStack(Material.AIR, 1));
                        choosingCombatant.setKit(1);
                        player.sendMessage("Kit Solider Equipped!");
                        break;
                    case BOW:
                        for(PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        player.getInventory().setHelmet(new ItemStack(Material.AIR));
                        player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                        player.getInventory().setItem(0, new ItemStack(Material.BOW, 1));
                        player.getInventory().setItem(1, new ItemStack(Material.ARROW, 16));
                        choosingCombatant.setKit(2);
                        player.sendMessage("Kit Archer Equipped!");
                        break;
                    case LEATHER_BOOTS:
                        for(PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        player.getInventory().setHelmet(new ItemStack(Material.AIR));
                        player.getInventory().setChestplate(new ItemStack(Material.AIR));
                        player.getInventory().setLeggings(new ItemStack(Material.AIR));
                        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                        player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD, 1));
                        player.getInventory().setItem(1, new ItemStack(Material.AIR, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000000,0));
                        choosingCombatant.setKit(3);
                        player.sendMessage("Kit Ninja Equipped!");
                        break;
                    case DIAMOND_CHESTPLATE:
                        for(PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        player.getInventory().clear();
                        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                        player.getInventory().setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
                        player.getInventory().setItem(1, new ItemStack(Material.AIR, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 6000000,0));
                        choosingCombatant.setKit(4);
                        player.sendMessage("Kit Tank Equipped!");
                        player.closeInventory();
                        break;
                    default:
                        return;
                }
            }
            main.combatantMap.put(player, choosingCombatant);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        Combatant respawningCombatant = main.combatantMap.get(player);
        int previousKit = respawningCombatant.getKit();
        switch (previousKit) {
            case 4:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 6000000, 0));
                break;
            case 3:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000000, 0));
                break;
            case 2:
                break;
            case 1:
                break;
            default:
                player.sendMessage("Select a kit using /kit");
        }
    }
}
