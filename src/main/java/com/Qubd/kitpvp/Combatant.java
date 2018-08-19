package com.Qubd.kitpvp;


import java.math.BigDecimal;
import java.util.UUID;

public class Combatant {
    private UUID uuid;
    private int level,gold,kills,deaths,kit;
    private BigDecimal totalExp;

    public Combatant(UUID uuid, int kit, int level, int gold, BigDecimal totalExp, int kills, int deaths) {
        this.uuid = uuid;
        this.kit = kit;
        this.level = level;
        this.gold = gold;
        this.totalExp = totalExp;
        this.kills = kills;
        this.deaths = deaths;
    }

    public int getKit() {
        return kit;
    }

    public void setKit(int kit) {
        this.kit = kit;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public BigDecimal getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(BigDecimal totalExp) {
        this.totalExp = totalExp;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
