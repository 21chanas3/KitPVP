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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


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
        main.buildSidebar(victim);
        main.buildSidebar(killer);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws IOException {
        final Player player = e.getPlayer();
        if (main.getDatabaseManager().playerExists(player.getUniqueId())) {
            ResultSet resultSet = main.getDatabaseManager().getPlayer(player.getUniqueId());
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
                main.combatantMap.put(player,joinedCombatant);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else {
            main.getDatabaseManager().createPlayer(player.getUniqueId());
            Combatant joinedCombatant = new Combatant(player.getUniqueId(),1,1,50,BigDecimal.ZERO,0,0);
            main.combatantMap.put(player,joinedCombatant);
        }
        main.buildSidebar(player);
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        Combatant leavingCombatant = main.combatantMap.get(player);
        final UUID uuid = leavingCombatant.getUuid();
        final int kit = leavingCombatant.getKit();
        final int level = leavingCombatant.getLevel();
        final int gold = leavingCombatant.getGold();
        final BigDecimal totalExp = leavingCombatant.getTotalExp();
        final int deaths = leavingCombatant.getDeaths();
        final int kills = leavingCombatant.getKills();
        main.getDatabaseManager().updatePlayer(uuid,kit,level,gold,totalExp,kills,deaths);
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
