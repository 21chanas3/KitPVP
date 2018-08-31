package com.Qubd.kitpvp;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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

import static org.bukkit.Bukkit.getServer;


public class EventListeners implements Listener {
    private Main main;

    public EventListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerGettingHungry(FoodLevelChangeEvent e){
        e.setCancelled(true);
    }
    @EventHandler
    public void onPlayerDeath(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            if (e.getDamage() >= ((Player) e.getEntity()).getHealth()) {
                e.setDamage(0.0);
                final Player victim = (Player) e.getEntity();
                final Player killer = (Player) e.getDamager();

                Combatant killedCombatant = main.combatantMap.get(victim.getUniqueId());
                Combatant killerCombatant = main.combatantMap.get(killer.getUniqueId());

                killedCombatant.setDeaths(killedCombatant.getDeaths() + 1);
                killerCombatant.setKills(killerCombatant.getKills() + 1);
                System.out.println("Killed" + killedCombatant.getLevel());
                System.out.println("Killer" + killerCombatant.getLevel());
                int difference = killedCombatant.getLevel() - killerCombatant.getLevel();
                double multiplier;
                if (difference <= -5) {
                    multiplier = 0.0;
                } else if (difference >= 5) {
                    multiplier = 2.0;
                } else {
                    multiplier = (0.2 * difference) + 1;
                }

                final int goldToAdd = (int) Math.round(5 * multiplier);
                System.out.println(difference);
                System.out.println(multiplier);
                System.out.println(goldToAdd);
                killerCombatant.setGold(killerCombatant.getGold() + goldToAdd);

                final double expToAdd = multiplier * 10;
                System.out.println(expToAdd);
                double newTotalExp = killerCombatant.getTotalExp().doubleValue() + expToAdd;
                int newLevel = (int) newTotalExp / 100;
                double percentageOfExpToNextLevel = (newTotalExp % 100) / 100;
                if (Math.floor(newTotalExp / 100) > killerCombatant.getLevel()) {
                    killer.sendMessage(ChatColor.LIGHT_PURPLE.toString()+ChatColor.STRIKETHROUGH+"-----------------------------------------------------");
                    sendCenteredMessage(killer, "&b&lLevel Up!");
                    killer.sendMessage(ChatColor.BLACK.toString());
                    sendCenteredMessage(killer, "[&dLv."+killerCombatant.getLevel()+"&r] &b-> &r[&dLv."+(killerCombatant.getLevel()+1)+"&r]");
                    killer.sendMessage(ChatColor.LIGHT_PURPLE.toString()+ChatColor.STRIKETHROUGH+"-----------------------------------------------------");
                    killerCombatant.setLevel(killerCombatant.getLevel() + 1);
                }

                BigDecimal convertedTotalExp = new BigDecimal(newTotalExp);
                killerCombatant.setTotalExp(convertedTotalExp);

                killer.setLevel(newLevel);
                killer.setExp((float) percentageOfExpToNextLevel);

                Bukkit.broadcastMessage(killer.getDisplayName() + " killed " + victim.getDisplayName());
                victim.sendMessage(ChatColor.RED + "You have been killed!");
                final double spawnx = main.config.getDouble("spawnx");
                final double spawny = main.config.getDouble("spawny");
                final double spawnz = main.config.getDouble("spawnz");
                final String world = main.config.getString("world");

                victim.setHealth(victim.getMaxHealth());
                victim.teleport(new Location(getServer().getWorld(world),spawnx,spawny,spawnz));

                main.buildSidebar(victim);
                main.buildSidebar(killer);

                return;
            }
        }
        return;
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
                main.combatantMap.put(player.getUniqueId(),joinedCombatant);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else {
            main.getDatabaseManager().createPlayer(player.getUniqueId());
            Combatant joinedCombatant = new Combatant(player.getUniqueId(),1,1,50,BigDecimal.ZERO,0,0);
            main.combatantMap.put(player.getUniqueId(),joinedCombatant);
        }
        main.buildSidebar(player);
        return;
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        Combatant leavingCombatant = main.combatantMap.get(player.getUniqueId());
        final UUID uuid = leavingCombatant.getUuid();
        final int kit = leavingCombatant.getKit();
        final int level = leavingCombatant.getLevel();
        final int gold = leavingCombatant.getGold();
        final BigDecimal totalExp = leavingCombatant.getTotalExp();
        final int deaths = leavingCombatant.getDeaths();
        final int kills = leavingCombatant.getKills();
        main.getDatabaseManager().updatePlayer(uuid,kit,level,gold,totalExp,kills,deaths);
        main.combatantMap.remove(player);
        return;
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Combatant choosingCombatant = main.combatantMap.get(player.getUniqueId());

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
            main.combatantMap.put(player.getUniqueId(), choosingCombatant);
            player.closeInventory();
        }
        return;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        Combatant respawningCombatant = main.combatantMap.get(player.getUniqueId());
        int previousKit = respawningCombatant.getKit();
        System.out.println(previousKit);
        switch (previousKit) {
            case 4:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 6000000, 0));
                break;
            case 3:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000000, 0));
                break;
            default:
                System.out.println("default");
                break;
        }
        return;
    }

    private final static int CENTER_PX = 154;

    public static void sendCenteredMessage(Player player, String message){
        if(message == null || message.equals("")) player.sendMessage("");
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
    }
}
